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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
import org.apache.nifi.processor.util.StandardValidators;

@Tags({ "attribute, content" })
@CapabilityDescription("Copies the content to an attribute.")
@SeeAlso()
@ReadsAttributes({ @ReadsAttribute(attribute = "") })
@WritesAttributes({ @WritesAttribute(attribute = "") })
@SideEffectFree
public class ContentToAttribute extends AbstractProcessor {
	
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success").description("success").build();

	private static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure").description("failure").build();
	
	private static final PropertyDescriptor ATTRIBUTE = new PropertyDescriptor.Builder()
	        .name("The attribute")
	        .required(true)
	        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
	        .build();

	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
	
	private final AtomicReference<String> value = new AtomicReference<>(null);
	
	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
		
		List<PropertyDescriptor> properties = new ArrayList<>();
	    properties.add(ATTRIBUTE);
	    descriptors = Collections.unmodifiableList(properties);
	    
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
			
			final String attribute = ctx.getProperty(ATTRIBUTE).getValue();

			flowFile = session.write(flowFile, (inputStream, outputStream) -> {
				
				final String input = IOUtils.toString(inputStream, Charset.forName("UTF-8"));
				value.set(input);
				
            });
			
			session.putAttribute(flowFile, attribute, value.get().substring(0, 1024));

			session.transfer(flowFile, REL_SUCCESS);
			
		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to copy attribute to content. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
