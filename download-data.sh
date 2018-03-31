#!/bin/bash

# Here for convenience. Some of these files may not be required.

mkdir -p files

# Download the Apache Joshua en-de archive.
wget https://www.dropbox.com/sh/3uag3az9imyih0x/AADC0g3jnBiVM2KmoJCeFsapa/apache-joshua-en-de-2017-01-31-phrase.tgz
gunzip -c apache-joshua-en-de-2017-01-31-phrase.tgz | tar xvf -
mv apache-joshua-en-de-2017-01-31 ./files/apache-joshua-en-de-2017-01-31
rm apache-joshua-en-de-2017-01-31-phrase.tgz

# Download the Apache Joshua de-en archive.
wget https://www.dropbox.com/sh/3uag3az9imyih0x/AAABtXI87ldqGxYOvBiHSbt_a/apache-joshua-de-en-2016-11-18.tgz
gunzip -c apache-joshua-de-en-2016-11-18.tgz | tar xvf -
mv apache-joshua-de-en-2016-11-18 ./files/apache-joshua-de-en-2016-11-18
rm apache-joshua-de-en-2016-11-18.tgz

# Download the index of a Wikipedia subset of German articles.
wget https://www.dropbox.com/s/07gx8snoxfy0iwi/de-wikipedia-lucene-index.zip -O index.zip
unzip index.zip -d ./files/de-wikipedia-lucene-index
rm index.zip



