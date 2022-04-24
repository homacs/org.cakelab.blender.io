#!/bin/bash
#
# This script uses a visual merge tool to display differences between 
# blender-latest and blender-previous version.
#


JAVA_BLEND_TOOLING="../org.cakelab.blender.io.tooling"

source "$JAVA_BLEND_TOOLING/sh/config.sh"   || exit -1
source "work/update.sh"  || exit -1



# LOCATION
# Target location of the cloned working copy which will become:
#     $LOCATION/blender 
LOCATION="$BLENDER_REPO_HOME"




function main ()
{
	update_show_DNA_diff	
}


main "$@"
