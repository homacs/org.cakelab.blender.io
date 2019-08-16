


Generating Library and Examples for a new Blender Release
=========================================================



I. Install new Blender Version
------------------------------
1. Download from http://blender.org and unzip.
2. Do a test run



II. Extract Python API
----------------------
Related to JavaBlendDocs/extract-pyapi-docs.sh

1. Adjust path to blender base dir in this script
2. Execute script

Result: New doc file "resources/dnadoc/VERSION/pyapi/doc.json"



III. Update Blender Source Code
-------------------------------

1. Identify the required version on https://developer.blender.org/diffusion/
2. Switch to our existing workspace "blender" or follow https://wiki.blender.org/index.php/Dev:Doc/Tools/Git to checkout repo
   - look for a branch called
        blender-vX.XX-release
   - checkout or switch to new branch
3. Identify changes:
   - check source/blender/makesdna/DNA_ID.h


IV. Create Source Code Docs
---------------------------
Refers to script "JavaBlendDocs/doxygen-dnasrcdoc-xml.sh"
1. Adjust version number in script!
2. Adjust ENV_PATH_BASE in script if necessary.
3. Execute script.

Result: New doc file "resources/dnadoc/VERSION/dnasrc/doc.json"



V. Copy added Documentation
---------------------------
1. Go in JavaBlendDocs/resources/dnadoc
2. Copy folder PREVIOUS_VER/added to NEW_VER/added
3. Review the content and compare with docs in dnasrc and pyapi


VI. Do a Test-Run with Class Generator
--------------------------------------
1. Get a .blend file of the new version (e.g. save the default file) 
2. Copy that file to org.cakelab.blender.io/versions/NEW_VERSION.blend (its temporary for now)
3. Add a test project as target for generated classes
4. Adjust Launcher or script to do a test generation run into that target project
5. Execute generator
6. Adjust versions/NEW_VERSION.blend according to version range given in output of generator
7. Validate generated output

VII. Update and Test Demo Application
------------------------------------
1. Update the launcher or script to write generated classes to JavaBlendViewer/gen
2. Open a shell (!) and remove the content of JavaBlendViewer/gen
3. Run generator to generate dna classes.
4. Open the example .blend files in JavaBlendViewer/examples in new Blender version and save them (now converted)
5. Refresh package view in IDE (so it actually sees changes)
6. Test viewer
7. Test Blender2Json converter
8. Test copy buffer exchange


VIII. Create new DNA Lib
------------------------
1. Open org.cakelab.blender.io/export-DNA-lib.xml
2. Scroll to target package-all
3. Add a new subsection for the new version (copy from previous) and adjust version number and file name.
4. Execute in console (!) 
      ant -f export-DNA-lib.xml
5. (just in case) Review output in content of the new files 
	- JavaBlend-1.1.0-DNA-2.79.jar
	- JavaBlend-src-1.1.0-DNA-2.79.zip
   in /home/homac/tmp/public_html/projects/JavaBlend/downloads
6. Create a test project and copy content of JavaBlendViewer except source folder "gen"
7. Add new library and do a test run
8. Upload new files to cakelab.org:public_html/projects/JavaBlend/downloads


IX. Commit Changes
------------------


1. JavaBlendDocs
  * commit & push
  * pull
2. JavaBlendViewer
  * Remove Blenders temp files: *.blend1
  * then commit
  * update
3. org.cakelab.blender.io
  * commit
  * update



X. Publish
---------------
1. org.cakelab.blender.io
  + update doc/index.html
  + run projexp
  + upload changes
2. JavaBlendViewer
  + update doc/index.html
  + run projexp
  + run ant -f export-src.xml
  + upload changes

