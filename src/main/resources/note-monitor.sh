#!/bin/bash

BASE_PATH='/Users/zxm/Documents/每日计划'
files=`ls $BASE_PATH`

for file in $files
do
	PARAM=$PARAM,$BASE_PATH/$file
done
PARAM=`echo ${PARAM#*,}`
echo $PARAM
java -cp /Users/zxm/tools/save-stickies/lib/evernote-api-1.25.1.jar:/Users/zxm/tools/save-stickies/note-collector.jar com.zxm.everynote.tools.HelloEveryNote $PARAM
