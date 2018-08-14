/*
 * (C) Copyright 2018 Mountain Fog, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mtnfog;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import com.mtnfog.utils.WikiClean;
import com.mtnfog.utils.WikiClean.WikiLanguage;

@Tags({ "search, wikipedia, wikitext" })
@CapabilityDescription("Removes WikiText formatting.")
@SeeAlso()
@ReadsAttributes({ @ReadsAttribute(attribute = "") })
@WritesAttributes({ @WritesAttribute(attribute = "") })
@SideEffectFree
public class WikiTextFilter extends AbstractProcessor {
	
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success").description("success").build();

	private static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure").description("failure").build();

	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
		
	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
		
	}
		
	@Override
	public Set<Relationship> getRelationships() {
		return relationships;
	}

	@Override
	public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public void onTrigger(final ProcessContext ctx,	final ProcessSession session) throws ProcessException {

		FlowFile flowFile = session.get();

		if (flowFile == null) {
			return;
		}

		try {

			flowFile = session.write(flowFile, (inputStream, outputStream) -> {

                final String input = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                
                String[] lines = input.split(System.getProperty("line.separator"));
                
                StringBuilder formatted = new StringBuilder();
                
                for(String line : lines) {
                	if(!line.startsWith("=") && !line.startsWith("*")) {
                		formatted.append(line);
                	}
                }            
                
                WikiClean cleaner =
                	    new WikiClean.Builder()
                	        .withLanguage(WikiLanguage.EN)
                	        .withTitle(false)
                	        .withFooter(false).build();
                	String content = cleaner.clean(formatted.toString());                
                
                content = content.replaceAll("Primary sources", "");
                content = content.replaceAll("Secondary sources", "");
                content = content.replaceAll("\\(.*\\)", "");
               // content = content.replaceAll("[^\\x00-\\x7F]", "");
                	
                IOUtils.write(content.toString(), outputStream, Charset.forName("UTF-8"));

            });

			session.transfer(flowFile, REL_SUCCESS);

		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to detect language. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
