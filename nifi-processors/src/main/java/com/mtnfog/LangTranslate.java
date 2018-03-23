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

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.joshua.decoder.Decoder;
import org.apache.joshua.decoder.JoshuaConfiguration;
import org.apache.joshua.decoder.StructuredTranslation;
import org.apache.joshua.decoder.Translation;
import org.apache.joshua.decoder.segment_file.Sentence;
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
import org.apache.nifi.processor.io.StreamCallback;

import com.google.gson.Gson;

@Tags({ "joshua, nlp, translate" })
@CapabilityDescription("Performs language translation using Apache Joshua.")
@SeeAlso({})
@ReadsAttributes({ @ReadsAttribute(attribute = "", description = "") })
@WritesAttributes({ @WritesAttribute(attribute = "", description = "") })
@SideEffectFree
public class LangTranslate extends AbstractProcessor {
	
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success").description("success").build();

	public static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure").description("failure").build();

	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
	
	private Decoder deDecoder;
	private int counter = 0;
	
	private Gson gson;
	
	private static final String JOSHUA_PATH = "/mtnfog/code/github/multilanguage-search/files";
	
	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<Relationship>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
			
		gson = new Gson();
		
		try {
		
			// TODO: Load stuff here.
			String deEnJoshuaConfigFile = JOSHUA_PATH + "/apache-joshua-en-de-2017-01-31/joshua.config";
			JoshuaConfiguration deEnConf = new JoshuaConfiguration();
			deEnConf.readConfigFile(deEnJoshuaConfigFile);
			deEnConf.use_structured_output = true;
			deEnConf.modelRootPath = JOSHUA_PATH + "/apache-joshua-en-de-2017-01-31/";
		
			deDecoder = new Decoder(deEnConf, deEnJoshuaConfigFile);
			
		} catch (IOException ex) {
			
			getLogger().error("Unable to initialize langtranslate processor: " + ex.getMessage());
			
		}
		
	}
	
	 protected FilenameFilter getJarFilenameFilter(){
		 return (dir, name) -> (name != null && name.endsWith(".bin"));
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

			flowFile = session.write(flowFile, new StreamCallback() {

				@Override
				public void process(InputStream inputStream, OutputStream outputStream) throws IOException {

					final String input = IOUtils.toString(inputStream, Charset.forName("UTF-8"));

					Sentence sentence = new Sentence(input, counter++, deDecoder.getJoshuaConfiguration());
					Translation translation = deDecoder.decode(sentence);
					List<StructuredTranslation> translations = translation.getStructuredTranslations();
					
					List<String> t = new LinkedList<>();
					t.add(input);
					
					for (StructuredTranslation st : translations) {
						t.add(st.getTranslationString());
					}
					
					final String json = gson.toJson(t);
					
					IOUtils.write(json, outputStream, Charset.forName("UTF-8"));							

				}

			});

			session.transfer(flowFile, REL_SUCCESS);

		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to detect language. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
