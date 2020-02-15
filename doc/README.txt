


Generating Library and Examples for a new Blender Release
=========================================================



I. Install new Blender Version
------------------------------
1. Download from http://blender.org and unzip.
2. Do a test run



II. Extract Python API
----------------------
Refers to JavaBlendDocs/extract-pyapi-docs.sh

1. Adjust path to blender base dir (BLENDER_BASE) in that script
2. Execute script in shell
   > cd <path-to>/JavaBlendDocs
   > ./extract-pyapi-docs.sh

Result: New doc file "resources/dnadoc/VERSION/pyapi/doc.json"



III. Update Blender Source Code
-------------------------------

1. Identify the required version on https://developer.blender.org/diffusion/
2. Switch to our existing workspace "blender" or follow https://wiki.blender.org/index.php/Dev:Doc/Tools/Git to checkout repo
   - look for a branch called
        blender-vX.XX-release
   - checkout or switch+pull to new branch
3. Identify changes for the change log on web page:
   - check source/blender/makesdna/DNA_ID.h


IV. Create Source Code Docs
---------------------------
Refers to script "JavaBlendDocs/doxygen-dnasrcdoc-xml.sh"
1. Adjust version number in script (BLENDER_VERSION)!
2. Adjust ENV_PATH_BASE in script if necessary.
3. Execute script.
	> cd <path-to>/JavaBlendDocs
	> ./doxygen-dnasrcdoc-xml.sh

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
3. Add a temporary test project as target for generated classes
4. Adjust Eclipse run configuration or script to do a test generation run into that target project
5. Execute generator
6. Adjust versions/NEW_VERSION.blend according to version range given in output of generator
7. Validate generated output

VII. Update and Test Demo Application
------------------------------------
1. Update the launcher or script to write generated classes to org.cakelab.blender.dna/src
2. Open a shell (!) and remove the content of org.cakelab.blender.dna/src (necessary to prevent git plugin to interfere)
3. Run generator to generate dna classes.
4. Open the example .blend files in org.cakelab.blender.viewer/examples in 
   new Blender version and save them (now converted).
   If files do not open or Blender freezes -> Uncheck "Load UI" in "Open File" dialog
5. Refresh package view in IDE (so it actually sees changes)
6. Test viewer
7. Test Blender2Json converter
8. Test copy buffer exchange


VIII. Create new DNA Lib
------------------------
1. Open org.cakelab.blender.io/export-DNA-lib.xml
2. [Adjust version of Java.Blend in init section] (only necessary on any changes in org.cakelab.blender.io)  
3. Scroll to target package-all
4. Add a new subsection for the new version (copy from previous) and adjust version number and file name.
5. Execute ant script from eclipse
    - make sure, ant executes with java 1.8 environment
    - Eclipse: Run As --> Ant Build ... -> configure JRE tab -> Run
    (alternatively console:  ant -f export-DNA-lib.xml)
6. (just in case) Review output in content of the new files 
	- JavaBlend-1.1.0-DNA-2.79.jar
	- JavaBlend-src-1.1.0-DNA-2.79.zip
   in /home/homac/tmp/public_html/projects/JavaBlend/downloads
7. Test demo apps against generated library
8. Upload new files to cakelab.org:public_html/projects/JavaBlend/downloads


IX. Commit Changes
------------------


1. JavaBlendDocs
  * commit & push
  * pull
2. org.cakelab.blender.viewer
  * Remove Blenders temp files: *.blend1 (not necessary -> .gitignore)
  * commit & push
  * pull
3. org.cakelab.blender.fileviewer
  * commit & push
  * pull
4. org.cakelab.blender.io
  * commit & push
  * pull
5. org.cakelab.blender.dna
  * commit & push
  * pull



X. Publish
---------------
1. org.cakelab.blender.io
  + update version in projexp.cfg
  + update doc/index.html
  + run projexp JavaBlend
  + review changes in browser
  + upload changes (scp -r tmp/public_html/projects/JavaBlend/* cakelab.org:public_html/projects/JavaBlend/.)
2. org.cakelab.blender.viewer
  + update doc/index.html
  + run projexp
  + run ant -f export-src.xml
  + upload changes
3. org.cakelab.blender.fileviewer
  + update doc/index.html
  + run projexp
  + run ant -f export-src.xml
  + upload changes
4. Commit final changes (web and scripts)
5. Mirror all projects to github
	



