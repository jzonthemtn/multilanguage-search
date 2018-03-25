# Multi-Language Search

This project has the ability to create a Lucene index from a Wikipedia dump that can be queried through a custom `QueryParser` that uses [Apache Joshua](https://cwiki.apache.org/confluence/display/JOSHUA/Apache+Joshua+%28Incubating%29+Home) to translate the search term to the language of the index. The project can be executed from the command line or in a NiFi pipline via the included NiFi processors.

The OpenNLP langdetect model is included in git lfs.

## Usage

First download the required data. Some of the data is quite big so it may take some time.

```
# ./download-data.sh
```

The `download-data.sh` script downloads the Apache Joshua `en-de` [language pack](https://cwiki.apache.org/confluence/display/JOSHUA/Language+Packs), a pre-built index of a subset of the German Wikipedia containing approximately 20,000 articles, and the [OpenNLP](https://opennlp.apache.org/) language detection model. These files are placed in the `./files` directory.

Now export the path to the data.

```
CUR_DIR=`pwd`
export MLS_HOME="$CUR_DIR/files"
```

### NiFi

Here's an example flow using the processors. Note that in this example the `langdetect` processor isn't really doing a whole lot since only a single index is searched but that's easily made more robust.

![Flow](https://raw.githubusercontent.com/jzonthemtn/multilanguage-search/master/example-nifi-flow.png)

To run the multi-language search in a NiFi dataflow:

```
# mvn clean install
# find . -name "*.nar" -exec cp {} /opt/nifi/lib/
```

Modify NiFI's `bootstrap.conf` to increase the `Xmx` parameter to `8g`. This is required to load the Apache Joshua model(s) and Wikipedia index searcher in memory.

Now start NiFi and create your dataflow. The NiFi processors in this project are:

* `langdetect-processor` - This processor uses [OpenNLP](https://opennlp.apache.org/)'s language detection capability to identify the language of the input text.

### CLI

To run from the command line:

```
# mvn clean install
# cd multilanguage-search-runner
# ./run-mls.sh
reading Lucene index
reading Joshua config
setting up decoders
........10........20........30........40........50........60........70........80........90.....100%
setting up OpenNLP lang detect
initalizing query parser
startup complete
...
birthday
detected language eng for query 'birthday'
found 1 translations
Geburtstag
query 'birthday' was parsed as text:birthday text:geburtstag
...
1. Washington’s Birthday (de)
2. Geburtstag (de)
3. Eubie Blake (de)
4. Maulid an-Nabī (de)
5. Maria Gaetana Agnesi (de)
...
```

The `run-wikipediaindexer.sh` script creates a Wikipedia index file if you do not use the one downloaded via the `download-data.sh` script.

The `run-mls.sh` script loads the index and allows querying.

## Credits

Credit to [@tteofili](https://github.com/tteofili) for the initial multilang-search implementation.

## License

Licensed under the Apache Software License, v2.
