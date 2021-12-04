#!/bin/bash
#
# This script uses a visual merge tool to display differences between 
# blender-latest and blender-previous version.
#


# LOCATION
# Target location of the cloned working copy which will become:
#     $LOCATION/blender 
LOCATION="$HOME/repos/git/blender.org"


SUBDIR=source/blender/makesdna


function fatal_exit ()
{
	echo "aborted." >&2
	exit 127
}


function error_exit ()
{
	echo "$1" >&2
	fatal_exit
}


function main ()
{
	
	pushd "$LOCATION" >/dev/null
	meld blender/$SUBDIR blender-previous/$SUBDIR
}


main "$@"
