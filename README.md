# Multi-Language Search

[![Build Status](https://travis-ci.org/jzonthemtn/multilanguage-search.svg?branch=master)](https://travis-ci.org/jzonthemtn/multilanguage-search)

This project demonstrates how multilanguage-search can be accomplished using Apache NiFi and Apache OpenNLP.

This project has the ability to create a Lucene index from a Wikipedia dump that can be queried through a custom `QueryParser` that uses [Apache Joshua](https://cwiki.apache.org/confluence/display/JOSHUA/Apache+Joshua+%28Incubating%29+Home) to translate the search term to the language of the index. The project can be executed from the command line or in a NiFi pipline via the included NiFi processors.

For better performance and more real-world applicability, the NiFi flow created from this project uses an Elasticsearch index of a subnet of the Wikipedia dumps instead of the generated Lucene index. It also utilizes Apache Joshua's REST interface to perform language translation. Both Elasticsearch and Apache Joshua's HTTP server need to be running in order for the flow to execute successfully.

## Apache NiFi Flow

Here's an example flow using the processors in this repository. The flow reads files of search terms from the file system and follows an "English in, English out" approach.

![Flow](https://raw.githubusercontent.com/jzonthemtn/multilanguage-search/master/flow.png)

### Walkthrough of NiFi Flow

The flow reads files from the file system containing a list of search terms (one per line). These are expected to be English search terms. The English term and its translated German equivalent term is used to query an Elasticsearch index containing both English and German Wikipedia pages. The search result is extracted and parsed to remove some WikiText formatting. The language of the article is determined. If the language is English the search result is returned. If the language is German, the individual sentences in the article are extracted and German to English translation is performed on each sentence. The sentences are then reassembled in order and returned as the search result.

## Usage

To run the multi-language search in a NiFi dataflow (assuming Apache NiFi is located at `/opt/nifi`):

```
# mvn clean install
# cp nifi-processors-nar/target/multilanguage-search-nifi.nar /opt/nifi/lib/
```

Now start NiFi and create your dataflow.

### NiFi Processors

The NiFi custom processors in this project are:

* `langdetect-processor` - This processor uses [OpenNLP](https://opennlp.apache.org/)'s language detection capability to identify the language of the input text.
* `langtranslate-processor` - This processor uses Apache Joshua to translate the text. Note that this processor calls Joshua directly and requires loading the Apache Joshua Language Pack in NiFi's memory. If you use this processor you will likely need to increase NiFi's memory in the `bootstrap.conf`.
* `langtranslaterest-processor` - This processor uses Apache Joshua's REST interface to translate the text.
* `wikitextfilter-processor` - This processor removes some of the WikiText formatting to give better translation. (This can be improved and likely replaced by NiFi's `ReplaceText` processor.)
* `wikipedia-index-search` - This processor facilitates querying a Lucene index of Wikipedia. (This processor is not used in the example flow in favor of an external Elasticsearch index.)

## Credits

Credit to [@tteofili](https://github.com/tteofili) for the initial multilang-search implementation.

## License

Licensed under the Apache Software License, v2.
