# Multi-Language Search

This project creates a Lucene index from a Wikipedia dump that can be queried through a custom `QueryParser` that uses [Apache Joshua](https://cwiki.apache.org/confluence/display/JOSHUA/Apache+Joshua+%28Incubating%29+Home) to translate the search term to the language of the index.

## Scripts

The `download-data.sh` script downloads the Apache Joshua `en-de` [language pack](https://cwiki.apache.org/confluence/display/JOSHUA/Language+Packs), a pre-built index of a subset of the German Wikipedia containing approximately 20,000 articles, and the [OpenNLP](https://opennlp.apache.org/) language detection model. These files are placed in the `./files` directory.

The `run-wikipediaindexer.sh` script creates a Wikipedia index file if you do not use the one downloaded via the `download-data.sh` script.

The `run-mls.sh` script loads the index and allows querying.

## Usage

Example usage:

```
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

## Credits

Credit to [@tteofili](https://github.com/tteofili) for the initial implementation.
