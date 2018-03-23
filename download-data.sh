#!/bin/bash

mkdir -p files

# Download the Apache Joshua en-de archive.
wget https://www.dropbox.com/sh/3uag3az9imyih0x/AADC0g3jnBiVM2KmoJCeFsapa/apache-joshua-en-de-2017-01-31-phrase.tgz
gunzip -c apache-joshua-en-de-2017-01-31-phrase.tgz | tar xvf -
mv apache-joshua-en-de-2017-01-31 ./files/apache-joshua-en-de-2017-01-31
rm apache-joshua-en-de-2017-01-31-phrase.tgz

# Download the index of a Wikipedia subset of German articles.
wget https://www.dropbox.com/s/07gx8snoxfy0iwi/de-wikipedia-lucene-index.zip?dl=0 -O index.zip
unzip index.zip
mv index ./files/
rm index.zip



