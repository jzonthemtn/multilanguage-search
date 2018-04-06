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
package com.mtnfog.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mtnfog.LangDetect;
import com.mtnfog.SentenceExtract;

public class SentenceExtractTest {

    private TestRunner runner;

    @Before
    public void init() {
    	runner = TestRunners.newTestRunner(SentenceExtract.class);
    }

    @Test
    public void extract() throws IOException {

        InputStream content = new ByteArrayInputStream("Guten Morgen! Mir geht’s gut. Mir geht’s nicht gut.".getBytes());
        
        runner.enqueue(content);
        runner.run(1);
        runner.assertQueueEmpty();
        
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(LangDetect.REL_SUCCESS);
        assertTrue("1 match", results.size() == 1);
        MockFlowFile result = results.get(0);

        final String output = IOUtils.toString(runner.getContentAsByteArray(result), "UTF-8");
                
        System.out.println(output);
        
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>(){}.getType();
        List<String> sentences = gson.fromJson(output, listType);
        
        assertEquals(3, sentences.size());
        
    }
 
}