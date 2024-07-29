#!/bin/bash
set -e

# Directory variable
if [ -d "/mnt/space/system" ]; then
    IDE_PLUGINS_DIR="/mnt/space/system/ide-plugins"
else
    IDE_PLUGINS_DIR="/mnt/jetbrains/system/ide-plugins"
fi

# Extract arguments
PLUGIN_NAME=$1
PLUGIN_URL=$2

mkdir -p $IDE_PLUGINS_DIR

echo "Download $PLUGIN_NAME plugin archive"
curl -s -L -o $IDE_PLUGINS_DIR/$PLUGIN_NAME.zip "$PLUGIN_URL"

echo "Unpack $PLUGIN_NAME plugin archive"
unzip $IDE_PLUGINS_DIR/$PLUGIN_NAME.zip -d $IDE_PLUGINS_DIR

echo "Remove $PLUGIN_NAME plugin archive"
rm $IDE_PLUGINS_DIR/$PLUGIN_NAME.zip
