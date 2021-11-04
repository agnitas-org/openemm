#!/bin/bash

function list_jars {
  echo $(echo "$1"/*.jar | tr ' ' ':')
}

function rename_jars {
    while read FILENAME
    do
	NEWNAME=`echo $FILENAME | sed -E "s/^(.*)\.jar$/\1.migrated.jar/"`
	mv "$FILENAME" "$NEWNAME"
    done < <( find "$1" -name "*.jar" )
}

function migrate {
    [ -e "$2/*" ] && rm "$2"/*
    
    java -jar `list_jars "$TOOL_LIB"` -profile=EE "$1" "$2"
    [ -e "$2"/scope.txt ] && rm "$2"/scope.txt

    rename_jars "$2"
}

TOOL_LIB=../lib_ant/jakartaee-migration-1.0.0

LIB_SOURCE=in.jee
LIB_DEST=out.jakarta

migrate "${LIB_SOURCE}" "${LIB_DEST}"
