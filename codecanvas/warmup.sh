#!/bin/bash
set -e

#Install plugins
./codecanvas/pluginsUtils.sh space "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=555267"
./codecanvas/pluginsUtils.sh com.intellij.ml.llm "https://plugins.jetbrains.com/plugin/download?rel=true&updateId=555384"

#Set Java options
export JAVA_OPTIONS='-Xmx1g -XX:+HeapDumpOnOutOfMemoryError'

#Build project
./gradlew build

