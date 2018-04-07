# Multi-Language Search

[![Build Status](https://travis-ci.org/jzonthemtn/multilanguage-search.svg?branch=master)](https://travis-ci.org/jzonthemtn/multilanguage-search)

This project demonstrates how multilanguage-search can be accomplished using Apache NiFi and Apache OpenNLP.

This project has the ability to create a Lucene index from a Wikipedia dump that can be queried through a custom `QueryParser` that uses [Apache Joshua](https://cwiki.apache.org/confluence/display/JOSHUA/Apache+Joshua+%28Incubating%29+Home) to translate the search term to the language of the index. The project can be executed from the command line or in a NiFi pipline via the included NiFi processors.

For better performance and more real-world applicability, the NiFi flow created from this project uses an Elasticsearch index of a subnet of the Wikipedia dumps instead of the generated Lucene index. It also utilizes Apache Joshua's REST interface to perform language translation. Both Elasticsearch and Apache Joshua's HTTP server need to be running in order for the flow to execute successfully.

## Apache NiFi Flow

Here's an example flow using the processors in this repository. The flow reads files of search terms from the file system and follows an "English in, English out" approach.

![Flow](https://raw.githubusercontent.com/jzonthemtn/multilanguage-search/master/flow.png)

### Walkthrough of NiFi Flow

1. The search terms are read from a file (`GetFile`). Each line of the file contains a search term in some language.
1. The file is split into separate flowfiles per line (`SplitText`) in order to process each search term.
1. An attribute is set (`UpdateAttribute`) on each flowfile that identifies the file that each search term came from.
1. The language of each search term is determined (`LangDetect`) and set as a new `language` attribute on the flowfile. This processor uses [OpenNLP](https://opennlp.apache.org/)'s `langdetect` capability.
1. The search term is made the content of the flowfile (`ExtractText`).
1. The search term is used to query an Elasticsearch index (`QueryElasticsearchHttp`). The processor determines which index to search based on the `language` attribute. For example, if the language is `eng` then the processor queries an English index of Wikipedia. If the language is `deu` then the processor queries a German index of Wikipedia.
1. Some WikiText syntax (headings, bulleted lists) is removed from the search results (`WikiTextFilter`).
1. The flowfile is now routed based on the `language` attribute.

For `eng`:
1. If it is `eng` the flow continues to the last processor for output.

For `deu`:
1. If the language is `deu` it is routed to a sentence extraction processor (`SentenceExtract`). This processor splits the search result text into its individual sentences via [OpenNLP](https://opennlp.apache.org/) and a German sentence model. The sentences are returned as a JSON array.
1. The JSON array of sentences is split into a flowfile per sentence (`SplitJson`).
1. Each sentence is now sent to Apache Joshua for translation (`LangTranslateRest-de-en`). This processor utilizes Apache Joshua's REST interface.
1. The resulting translations are combined together (`MergeContent`) to give the full translated search result.

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
