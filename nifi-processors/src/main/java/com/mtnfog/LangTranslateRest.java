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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.mtnfog.model.JoshuaResponse;
import com.mtnfog.rest.JoshuaService;
import com.mtnfog.model.Translation;

@Tags({ "joshua, nlp, translate" })
@CapabilityDescription("Performs language translation using Apache Joshua via REST.")
@SeeAlso()
@ReadsAttributes({ @ReadsAttribute(attribute = "") })
@WritesAttributes({ @WritesAttribute(attribute = "") })
@SideEffectFree
public class LangTranslateRest extends AbstractProcessor {
	
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success").description("success").build();

	private static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure").description("failure").build();

	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
	
	private static final PropertyDescriptor APACHE_JOSHUA_HOST = new PropertyDescriptor.Builder()
	        .name("Apache Joshua Path Host")
	        .required(true)
	        .defaultValue("http://localhost:5674/")
	        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
	        .build();
	
	private static final PropertyDescriptor INCLUDE_SCORE = new PropertyDescriptor.Builder()
	    .name("Include Score")
	    .required(true)
	    .defaultValue("true")
	    .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
	    .build();
	
	private final AtomicReference<String> originalQuery = new AtomicReference<>(null);
	
	private JoshuaService service;
	
	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
			
		List<PropertyDescriptor> properties = new ArrayList<>();
	    properties.add(APACHE_JOSHUA_HOST);
	    descriptors = Collections.unmodifiableList(properties);
		
	}
	
	@OnScheduled
	public void setup(ProcessContext context) {
		
		final OkHttpClient okHttpClient = new OkHttpClient.Builder()
        	.readTimeout(60, TimeUnit.SECONDS)
        	.connectTimeout(60, TimeUnit.SECONDS)
        	.build();

		Retrofit retrofit = new Retrofit.Builder()
	    	.baseUrl(context.getProperty(APACHE_JOSHUA_HOST).getValue())
	    	.client(okHttpClient)
	    	.addConverterFactory(GsonConverterFactory.create())
	    	.build();

		service = retrofit.create(JoshuaService.class);
		
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

                final String input = IOUtils.toString(inputStream, Charset.forName("UTF-8"));                                
                final String encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
                
                originalQuery.set(input);
                
                final boolean includeScore = ctx.getProperty(INCLUDE_SCORE).isSet();
                
                Call<JoshuaResponse> response = service.translate(encoded);
                List<Translation> translations = response.execute().body().getData().getTranslations();
                
                StringBuilder sb = new StringBuilder();

                for(Translation t : translations) {
                	
                	if(includeScore) {
                		sb.append(t.getTranslatedText() + " (score " + t.getRawNbest().get(0).getTotalScore().toString() + ") ");
                	} else {
                		sb.append(t.getTranslatedText());
                	}
                	
                }
                
                IOUtils.write(sb.toString(), outputStream, StandardCharsets.UTF_8);

            });

			session.transfer(flowFile, REL_SUCCESS);

		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to translate language. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
