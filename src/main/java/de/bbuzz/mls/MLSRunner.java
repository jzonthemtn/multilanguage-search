package de.bbuzz.mls;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import org.apache.joshua.decoder.Decoder;
import org.apache.joshua.decoder.JoshuaConfiguration;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

/**
 *
 */
public class MLSRunner {

  public static void main(String[] args) throws Exception {

    System.out.println("reading Lucene index");
    String indexPath = "/Users/teofili/Desktop/bbuzz-mls/index";
    Directory directory = FSDirectory.open(Paths.get(indexPath));
    DirectoryReader directoryReader = DirectoryReader.open(directory);
    IndexSearcher searcher = new IndexSearcher(directoryReader);

    try {

      System.out.println("reading Joshua config");

      String deEnJoshuaConfigFile = "/Users/teofili/Desktop/bbuzz-mls/language-packs/apache-joshua-en-de-2017-01-31/joshua.config";
      JoshuaConfiguration deEnConf = new JoshuaConfiguration();
      deEnConf.readConfigFile(deEnJoshuaConfigFile);
      deEnConf.use_structured_output = true;

      System.out.println("setting up decoders");

      // set up decoders
      Map<String, Collection<Decoder>> mappings = new HashMap<>();

      Collection<Decoder> deDecoders = new LinkedList<>();
      Decoder deDecoder = new Decoder(deEnConf, deEnJoshuaConfigFile);
      deDecoders.add(deDecoder);
      mappings.put("eng", deDecoders);

      System.out.println("setting up OpenNLP lang detect");
      // set up lang detect
      LanguageDetector languageDetector = new LanguageDetectorME(new LanguageDetectorModel(new FileInputStream("/Users/teofili/Desktop/bbuzz-mls/langdetect/lang-maxent.bin")));

      System.out.println("initalizing query parser");
      LanguageExpandingQueryParser languageExpandingQueryParser = new LanguageExpandingQueryParser("text", new StandardAnalyzer(), languageDetector, mappings);

      System.out.println("startup complete");

      Scanner scanner = new Scanner(System.in);

      System.out.println("...");

      while (true) {
        String userEnteredQuery = scanner.nextLine();
        if (userEnteredQuery.trim().length() > 0) {
          Query query = languageExpandingQueryParser.parse(userEnteredQuery);
          System.out.println("query '" + userEnteredQuery + "' was parsed as " + query);
          TopDocs topDocs = searcher.search(query, 5);
          System.out.println("...");
          int dn = 1;
          for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = directoryReader.document(scoreDoc.doc);
            System.out.println(dn + ". " + document.get("title") + " (" + document.get("lang") + ")");
            dn++;
          }
          System.out.println("...");
        }
      }

    } finally {
      directoryReader.close();
      directory.close();
    }
  }
}
