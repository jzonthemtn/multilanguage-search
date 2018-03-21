#!/bin/bash

# Download the Apache Joshua en-de archive.
wget https://mtnfog-public.s3.amazonaws.com/haystack-04-2018/apache-joshua-en-de-2017-01-31-phrase.tgz
gunzip -c apache-joshua-en-de-2017-01-31-phrase.tgz | tar xvf -
mv apache-joshua-en-de-2017-01-31 ./files/
rm apache-joshua-en-de-2017-01-31-phrase.tgz

# Download the index of a Wikipedia subset of German articles.
wget https://mtnfog-public.s3.amazonaws.com/haystack-04-2018/index.zip
unzip index.zip
mv index ./files/
rm index.zip

# Download the OpenNLP language detection model.
wget http://www.gtlib.gatech.edu/pub/apache/opennlp/models/langdetect/1.8.3/langdetect-183.bin -O ./files/


