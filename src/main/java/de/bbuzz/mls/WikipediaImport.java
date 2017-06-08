/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bbuzz.mls;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

/**
 * Utility class to import Wikipedia dumps into a Lucene index.
 */
public class WikipediaImport {

  private final File dump;
  private final boolean doReport;
  private final String languageCode;

  public WikipediaImport(File dump, String languageCode, boolean doReport) {
    this.dump = dump;
    this.languageCode = languageCode;
    this.doReport = doReport;
  }

  public void importWikipedia(IndexWriter indexWriter) throws Exception {
    long start = System.currentTimeMillis();
    int count = 0;
    if (doReport) {
      System.out.format("Importing %s...%n", dump);
    }

    String title = null;
    String text = null;
    XMLInputFactory factory = XMLInputFactory.newInstance();
    StreamSource source;
    if (dump.getName().endsWith(".xml")) {
      source = new StreamSource(dump);
    } else {
      CompressorStreamFactory csf = new CompressorStreamFactory();
      source = new StreamSource(csf.createCompressorInputStream(
          new BufferedInputStream(new FileInputStream(dump))));
    }
    XMLStreamReader reader = factory.createXMLStreamReader(source);
    while (reader.hasNext()) {
      switch (reader.next()) {
        case XMLStreamConstants.START_ELEMENT:
          if ("title".equals(reader.getLocalName())) {
            title = reader.getElementText();
          } else if ("text".equals(reader.getLocalName())) {
            text = reader.getElementText();
          }
          break;
        case XMLStreamConstants.END_ELEMENT:
          if ("page".equals(reader.getLocalName())) {
            Document page = new Document();
            page.add(new TextField("title", title, Field.Store.YES));
            page.add(new TextField("text", text, Field.Store.NO));
            page.add(new StringField("lang", languageCode, Field.Store.YES));
            indexWriter.addDocument(page);
            count++;
            if (count % 10000 == 0) {
              batchDone(indexWriter, start, count);
            }

            pageAdded(title, text);
          }
          break;
      }
    }

    indexWriter.commit();

    if (doReport) {
      long millis = System.currentTimeMillis() - start;
      System.out.format(
          "Imported %d pages in %d seconds (%.2fms/page)%n",
          count, millis / 1000, (double) millis / count);
    }

  }

  protected void batchDone(IndexWriter indexWriter, long start, int count) throws IOException {
    indexWriter.commit();
    if (doReport) {
      long millis = System.currentTimeMillis() - start;
      System.out.format(
          "Added %d pages in %d seconds (%.2fms/page)%n",
          count, millis / 1000, (double) millis / count);
    }
  }

  protected void pageAdded(String title, String text) {
  }

}