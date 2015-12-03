#!/bin/bash

#
# This is a script which calls blender with a blender standard
# python script to receive all docs from blender python API.
# Those are related to on RNA, but RNA is kind of derived from
# DNA.
#
# The output is written to stdout and should be piped into a file.
# The generated file can be fed to the class:
#     org.cakelab.blender.ExtractPyAPIDoc
# as input to generate a Java Blend documentation file (JSON).
#
# -homac
#



version=`blender -v | grep "Blender" | head -n 1 | awk '{print $2}'`


blender --background -noaudio --python /usr/share/blender/scripts/modules/rna_info.py 2> /tmp/rna_info-${version}.out >/dev/null

cat /tmp/rna_info-2.69.out | while read line && [ "$line" != "EOF" ] ; do 
	#
	# filter debug messages
	#
	if expr "$line" : "^[^\\.\\:]*:" >/dev/null; then 
		continue ;
	fi
	echo "$line"	
done


