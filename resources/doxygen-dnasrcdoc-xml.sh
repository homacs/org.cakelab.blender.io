#!/bin/bash
#
# This is a script which calls doxygen to produce xml output
# from blender dna header files.
# Unfortunately, I have deleted the xml2doc classes (have to 
# write them again).
#
#
# -homac
#





BLENDER_VERSION=2.69

export ENV_PATH_BASE="/home/homac/workspace-sb6jcode"
export ENV_INPUT="$ENV_PATH_BASE/blender-$BLENDER_VERSION/source/blender/makesdna"
export ENV_OUTPUT_DIR="/tmp/blender-$BLENDER_VERSION-xmldoc"


doxygen ./blender-${BLENDER_VERSION}-dnasrc-xml.doxygen
pushd $ENV_OUTPUT_DIR
xsltproc combine.xslt index.xml >/tmp/blender-$BLENDER_VERSION-all.xml
popd

