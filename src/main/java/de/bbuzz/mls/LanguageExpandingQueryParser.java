package de.bbuzz.mls;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.joshua.decoder.Decoder;
import org.apache.joshua.decoder.StructuredTranslation;
import org.apache.joshua.decoder.Translation;
import org.apache.joshua.decoder.segment_file.Sentence;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;

/**
 * A {@link QueryParser} that takes the original user entered query,
 * recognizes the language via openNLP's {@link LanguageDetector}, picks up the {@link Decoder}s that
 * can translate from the given language, perform decoding (translation), parses the translated
 * queries and then adds them as optional queries in a {@link BooleanQuery}.
 */
public class LanguageExpandingQueryParser extends QueryParser {

  private final LanguageDetector languageDetector;
  private final Map<String, Collection<Decoder>> decoderMappings;
  private int counter = 0;

  public LanguageExpandingQueryParser(String f, Analyzer a, LanguageDetector languageDetector,
                                      Map<String, Collection<Decoder>> decoderMappings) {
    super(f, a);
    this.languageDetector = languageDetector;
    this.decoderMappings = decoderMappings;
  }

  @Override
  public Query parse(String query) throws ParseException {

    BooleanQuery.Builder builder = new BooleanQuery.Builder();

    // add user entered query
    builder.add(new BooleanClause(super.parse(query), BooleanClause.Occur.MUST));

    // perform language detection
    Language language = languageDetector.predictLanguage(query);

    // find which joshua model supports the extracted language
    Collection<Decoder> decoders = decoderMappings.get(language.getLang());

    Sentence sentence = new Sentence(query, counter, null);
    counter++;

    // perform query translation for each of the joshua models
    for (Decoder d : decoders) {
      Translation translation = d.decode(sentence);
      List<StructuredTranslation> translations = translation.getStructuredTranslations();

      // create and bind corresponding queries
      for (StructuredTranslation st : translations) {
        // if the translation is good enough it gets parsed and added as a SHOULD query
        if (st.getTranslationScore() > 0.5f) {
          String translationString = st.getTranslationString();
          builder.add(new BooleanClause(super.parse(translationString), BooleanClause.Occur.SHOULD));
        }
      }
    }

    return builder.build();
  }

}
