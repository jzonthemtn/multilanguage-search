#!/bin/bash
DIR=`pwd`
export MLS_PATH="$DIR/files/"
java -jar ./target/wikipediaimport.jar
