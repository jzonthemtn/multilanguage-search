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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.wikiclean.WikiClean;
import org.wikiclean.WikiClean.WikiLanguage;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

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
		
	private LanguageDetector languageDetector;
	
	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
		
		try {
			
			final InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("langdetect-183.bin");
			final LanguageDetectorModel m = new LanguageDetectorModel(resourceAsStream);
			languageDetector = new LanguageDetectorME(m);
			
			resourceAsStream.close();

		} catch (IOException ex) {

			getLogger().error("Unable to initialize langdetect processor: " + ex.getMessage());

		}
		
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
                
                final Language[] languages = languageDetector.predictLanguages(input);
                final String language = languages[0].getLang();

                WikiClean cleaner = null;
                
                if(StringUtils.equalsIgnoreCase(language, "eng")) {
                	
                    cleaner =
                    	    new WikiClean.Builder()
                    	        .withLanguage(WikiLanguage.EN)
                    	        .withTitle(false)
                    	        .withFooter(false)
                    	        .build();
                	
                } else if(StringUtils.equalsIgnoreCase(language, "eng")) {
                	
                    cleaner =
                    	    new WikiClean.Builder()
                    	        .withLanguage(WikiLanguage.DE)
                    	        .withTitle(false)
                    	        .withFooter(false)
                    	        .build();
                    
                }
                
                final String content = cleaner.clean(input);
                
                IOUtils.write(content, outputStream, Charset.forName("UTF-8"));

            });

			session.transfer(flowFile, REL_SUCCESS);

		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to filter Wikipedia text. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
