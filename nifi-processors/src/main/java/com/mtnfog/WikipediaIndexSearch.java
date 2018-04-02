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
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Tags({ "search, lucene, wikipedia" })
@CapabilityDescription("Performs search against Wikipedia index.")
@SeeAlso()
@ReadsAttributes({ @ReadsAttribute(attribute = "") })
@WritesAttributes({ @WritesAttribute(attribute = "") })
@SideEffectFree
public class WikipediaIndexSearch extends AbstractProcessor {
	
	public static final Relationship REL_SUCCESS = new Relationship.Builder()
			.name("success").description("success").build();

	private static final Relationship REL_FAILURE = new Relationship.Builder()
			.name("failure").description("failure").build();

	private List<PropertyDescriptor> descriptors;
	private Set<Relationship> relationships;
		
	private Gson gson;
	private DirectoryReader directoryReader;
	private IndexSearcher searcher;
	
	private static final PropertyDescriptor WIKIPEDIA_INDEX_PATH = new PropertyDescriptor.Builder()
	        .name("Wikipedia Index Path")
	        .required(true)
	        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
	        .defaultValue("/opt/de-wikipedia-lucene-index")
	        .build();

	@Override
	protected void init(final ProcessorInitializationContext context) {

		relationships = new HashSet<>();
		relationships.add(REL_SUCCESS);
		relationships.add(REL_FAILURE);
		relationships = Collections.unmodifiableSet(relationships);
		
		List<PropertyDescriptor> properties = new ArrayList<>();
	    properties.add(WIKIPEDIA_INDEX_PATH);
	    descriptors = Collections.unmodifiableList(properties);
			
		gson = new Gson();
		
	}
	
	@OnScheduled
	public void setup(ProcessContext context) {

		final String wikipediaIndexPath = context.getProperty(WIKIPEDIA_INDEX_PATH).getValue();
		
		try {
			
		    Directory directory = FSDirectory.open(Paths.get(wikipediaIndexPath));
		    directoryReader = DirectoryReader.open(directory);
		    searcher = new IndexSearcher(directoryReader);
			
		} catch (IOException ex) {
			
			getLogger().error("Unable to initialize wikipedia index search processor: " + ex.getMessage());
			
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

                Type jsonString = new TypeToken<List<String>>(){}.getType();
                List<String> translations = gson.fromJson(input, jsonString);

                QueryParser queryParser = new QueryParser("text", new StandardAnalyzer());

                BooleanQuery.Builder builder = new BooleanQuery.Builder();

                try {

                for(String translation : translations) {
                    builder.add(new BooleanClause(queryParser.parse(translation), BooleanClause.Occur.SHOULD));
                }

                } catch (ParseException ex) {
                    getLogger().error("Unable to parse query term: " + ex.getMessage());
                }

                Query query = builder.build();

                TopDocs topDocs = searcher.search(query, 5);

                List<String> results = new LinkedList<>();

                int dn = 1;
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Document document = directoryReader.document(scoreDoc.doc);
                    System.out.println(dn + ". " + document.get("title") + " (" + document.get("lang") + ")");
                    results.add(document.get("title") + " (" + document.get("lang") + ")");
                    dn++;
                }

                IOUtils.write(gson.toJson(results), outputStream, Charset.forName("UTF-8"));

            });

			session.transfer(flowFile, REL_SUCCESS);

		} catch (Exception ex) {
			
			getLogger().error(String.format("Unable to detect language. Exception: %s", ex.getMessage()), ex);
			session.transfer(flowFile, REL_FAILURE);
			
		}

	}
	
}
