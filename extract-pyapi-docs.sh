#!/bin/bash

#
# This is a script which calls blender with a blender standard
# python script to receive all docs from blender python API.
# Those are related to on RNA, but RNA is kind of derived from
# DNA.
#
# The output of the script is fed to the class:
#     org.cakelab.blender.doc.extract.ExtractPyAPIDoc
# as input to generate a Java Blend documentation file (JSON).
#
# -homac
#


################# CONFIGURATION SECTION ################

#
# SCRIPT
# This the path to the python script called rna_info.py. It
# Reads the runtime API documentation and dumps it on stdout.
# We use the standard path on linux distros here. If it is not
# there then use the following command to locate it:
# > updatedb && locate rna_info.py
#
SCRIPT=/usr/share/blender/scripts/modules/rna_info.py

#
# OUTPUT
# This is the folder, where the Java Blend documentation system
# stores documentation for DNA structs. The documentation is
# used by the model generator to write it to generated classes.
# Default: "resources/dnadoc"
#
OUTPUT="resources/dnadoc"

#
# VERSION
# This is the version of the Blender installed on your system.
# This version information is used to create a subfolder in the
# documentation system. 
# We receive the version from the Blender process, but you can 
# even set a fixed version number.
#
VERSION=`blender -v | grep "Blender" | head -n 1 | awk '{print $2}'`

#
# TMP
# Temp directory to store temporary files.
#
TMP=/tmp


################# END OF CONFIGURATION SECTION ################




echo "extracting blenders python api documentation."
echo "blender version: ${VERSION}"
echo "running python script: ${SCRIPT}"



SCRIPT_OUTPUT=$TMP/pyapi-${VERSION}.txt
CONVERTER_INPUT=$TMP/pyapi-${VERSION}-clean.txt

rm -f $SCRIPT_OUTPUT
blender --background -noaudio --python $SCRIPT 2> $SCRIPT_OUTPUT >/dev/null

echo "removing debug output"
cat $SCRIPT_OUTPUT | while read line && [ "$line" != "EOF" ] ; do 
	#
	# filter debug output
	#
	if expr "$line" : "^[^\\.\\:]*:" >/dev/null; then 
		continue ;
	fi
	echo "$line"	
done > $CONVERTER_INPUT



echo "calling converter class: org.cakelab.blender.doc.extract.ExtractPyAPIDoc"

CLASSPATH=./bin:../org.cakelab.json/bin
CLASSPATH=${CLASSPATH}`find lib -name "*.jar" | while read jar ; do 
	echo -n ":$jar"
done`

java -cp ${CLASSPATH} org.cakelab.blender.doc.extract.ExtractPyAPIDoc -v ${VERSION} -in $CONVERTER_INPUT -out ${OUTPUT}

rm -f $SCRIPT_OUTPUT
rm -f $CONVERTER_INPUT
echo "done."