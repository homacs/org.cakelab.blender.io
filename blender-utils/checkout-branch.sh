#!/bin/bash
#
# This script clones a branch of the blender project from blender.org git repository.
# You have to customize LOCATION and BRANCH before running it.
#



# BRANCH
# Branch to be checked out. Blender creates a new branch for each release with following pattern:
#     blender-vX.XX-release
BRANCH=blender-v2.90-release


# LOCATION
# Target location of the cloned working copy which will become:
#     $LOCATION/blender 
LOCATION="$HOME/repos/git/blender.org"





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


function do_checkout ()
{
	git clone --branch "$BRANCH" git://git.blender.org/blender.git || error_exit "checkout failed"
	cd blender || fatal_exit
	git submodule update --init --recursive || fatal_exit
	git submodule foreach git checkout "$BRANCH" || fatal_exit
	git submodule foreach git pull --rebase origin "$BRANCH" || fatal_exit
}


function main ()
{
	cd "$LOCATION"  || fatal_exit
	! [ -d blender ] || error_exit "Directory 'blender' already exists."
	
	do_checkout
}


main "$@"