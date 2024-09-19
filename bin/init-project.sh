#!/bin/bash

# creates a maven project structure, assumes pom.xml is already there

if [ "$1" == "" ]; then
	echo "No package name provided."
	exit
else
	PACKAGE=$1
fi

if [ ! -f "pom.xml" ]; then
	echo "no project found; aborting."
	exit	
fi

mkdir -p src/{main,test}/{kotlin/com/enigmastation/streampack/${PACKAGE}/,resources}
mkdir -p src/{main,test}/kotlin/com/enigmastation/streampack/${PACKAGE}/{dto,entity,service,handler,operation,repository}
touch src/test/resources/application.properties
