#!/bin/bash
DIR=`pwd`
export MLS_PATH="$DIR/files/"
java -Xmx8g -jar ./target/mlsrunner.jar

