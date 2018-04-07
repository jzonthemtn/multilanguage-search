# Multi-Language Search

[![Build Status](https://travis-ci.org/jzonthemtn/multilanguage-search.svg?branch=master)](https://travis-ci.org/jzonthemtn/multilanguage-search)

This project demonstrates how multilanguage-search can be accomplished using Apache NiFi and Apache OpenNLP.

This project has the ability to create a Lucene index from a Wikipedia dump that can be queried through a custom `QueryParser` that uses [Apache Joshua](https://cwiki.apache.org/confluence/display/JOSHUA/Apache+Joshua+%28Incubating%29+Home) to translate the search term to the language of the index. The project can be executed from the command line or in a NiFi pipline via the included NiFi processors.

For better performance and more real-world applicability, the NiFi flow created from this project uses an Elasticsearch index of a subnet of the Wikipedia dumps instead of the generated Lucene index. It also utilizes Apache Joshua's REST interface to perform language translation. Both Elasticsearch and Apache Joshua's HTTP server need to be running in order for the flow to execute successfully.

### Apache NiFi Flow

Here's an example flow using the processors in this repository. The flow reads files of search terms from the file system and follows an "English in, English out" approach.

Each search term is translated to one of several languages. Each of these languages has a subset of Wikipedia indexed in a local Elasticsearch. The search results are returned and translated back to English.

![Flow](https://raw.githubusercontent.com/jzonthemtn/multilanguage-search/master/flow.png)

To run the multi-language search in a NiFi dataflow:

```
# mvn clean install
# cp nifi-processors-nar/target/multilanguage-search-nifi.nar /opt/nifi/lib/
```

Now start NiFi and create your dataflow. The NiFi custom processors in this project are:

* `langdetect-processor` - This processor uses [OpenNLP](https://opennlp.apache.org/)'s language detection capability to identify the language of the input text.
* `langtranslate-processor` - This processor uses Apache Joshua to translate the text. Note that this processor calls Joshua directly and requires loading the Apache Joshua Language Pack in NiFi's memory. If you use this processor you will likely need to increase NiFi's memory in the `bootstrap.conf`.
* `langtranslaterest-processor` - This processor uses Apache Joshua's REST interface to translate the text.
* `wikipedia-index-search` - This processor facilitates querying a Lucene index of Wikipedia. (This processor is not used in the example flow in favor of an external Elasticsearch index.)

## Credits

Credit to [@tteofili](https://github.com/tteofili) for the initial multilang-search implementation.

## License

Licensed under the Apache Software License, v2.
