#!/bin/bash
#
# This is a script to generate Java classes from a .blend file.
# By default, it uses information gathered from the installed 
# blender version.
#
# -homac
#


################# CONFIGURATION SECTION ################
BLENDER_DEVEL_ENV=false

# BLENDER
# Blender executable.
BLENDER=$HOME/opt/blender-2.80-linux-glibc217-x86_64/blender

if $BLENDER_DEVEL_ENV ; then
	# BLENDER_DEV_PATH
	# Path to .
	BUILD_PATH="/home/homac/repos/git/blender.org/blender/build"
	
	# BLENDER_SYSTEM_DATAFILES
	export BLENDER_SYSTEM_DATAFILES="$BUILD_PATH/release/datafiles"
	
	# BLENDER_SYSTEM_SCRIPTS
	export BLENDER_SYSTEM_SCRIPTS="$BUILD_PATH/../release/scripts"
	
	# LD_LIBRARY_PATH
	export LD_LIBRARY_PATH="$BUILD_PATH/lib"
fi


#
# VERSION
# This is the Blender version in which the INPUT file was saved 
# (see INPUT below). We receive the version from the Blender process
# but you can even set a fixed version number.
#
VERSION=`$BLENDER -v | grep "Blender" | head -n 1 | awk '{print $2}'`

#
# INPUT 
# This is a Blender file (.blend) which was saved by a Blender 
# process in the version you want to support.
# We use the file with the user preferences as default but you
# can use any other .blend file as well.
#
#INPUT="${HOME}/.config/blender/${VERSION}/config/userpref.blend"
INPUT="./versions/2.80.75-2.80.0.blend"

#
# OUTPUT
# This is the target folder, where the generated classes will
# be stored. It will be extended by the Java package folders 
# according to the PACKAGE argument (see below).
#
#OUTPUT="/home/homac/workspaces/Java.Blend/jb-test"
OUTPUT="../JavaBlendViewer/gen"

#
# DOCPATH
# This is the location of the Java .Blend externally maintained
# source code documentation. 
#
DOCPATH="$HOME/repos/git/github/homacs/JavaBlendDocs/resources/dnadoc"

#
# PACKAGE
# This is the Java package for all generated classes. If you are
# going to support import/export of .blend files of different 
# versions, then you should consider adding a postfix such as:
# PACKAGE="org.blender.v269".
#
PACKAGE="org.blender"



################# END OF CONFIGURATION SECTION ################


echo "running model generator"


CLASSPATH=./bin:../org.cakelab.json/bin
CLASSPATH=${CLASSPATH}`find lib -name "*.jar" | while read jar ; do 
	echo -n ":$jar"
done`



java -cp ${CLASSPATH} org.cakelab.blender.generator.ModelGenerator -in ${INPUT} -c ${DOCPATH} -out ${OUTPUT} -p ${PACKAGE}


echo "done."