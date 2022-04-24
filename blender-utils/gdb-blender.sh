#!/bin/bash

JAVA_BLEND_TOOLING="../org.cakelab.blender.io.tooling"

source "$JAVA_BLEND_TOOLING/sh/config.sh"   || exit -1
source "blender/repo.sh"  || exit -1



BUILD=$BLENDER_REPO_HOME/blender/build

CFGDIR=$HOME/tmp/.blender

BLENDER_USER_CONFIG=$CFGDIR
BLENDER_USER_SCRIPTS=$CFGDIR
BLENDER_SYSTEM_SCRIPTS=$CFGDIR
BLENDER_USER_DATAFILES=$CFGDIR
BLENDER_SYSTEM_PYTHON==$CFGDIR

LD_LIBRARY_PATH=$BUILD/lib

gdb $BUILD/bin/blender