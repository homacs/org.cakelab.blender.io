


Generating Library and Examples for a new Blender Release
=========================================================



I. Install new Blender Version
------------------------------
1. Download from http://blender.org and unzip.
2. Do a test run (important)



II. Extract Python API
----------------------
Refers to org.cakelab.blender.dnadoc/extract-pyapi-docs.sh

1. Adjust path to blender base dir (BLENDER_BASE) in that script (if necessary)
2. Execute script in shell
   > cd <path-to>/org.cakelab.blender.dnadoc
   > ./extract-pyapi-docs.sh

Result: New doc file "resources/dnadoc/VERSION/pyapi/doc.json"



III. Update Blender Source Code
-------------------------------
Refers to script org.cakelab.blender.io/blender-utils/checkout-branch.sh

1. Identify the required branch of the new version on https://developer.blender.org/diffusion/B/branches/master/
   --> blender-vX.XX-release
   Note: For patch version updates (tags, e.g. 2.90.0 -> 2.90.1), just 'git pull' the branch
2. Clone the new branch  (or:  https://wiki.blender.org/index.php/Dev:Doc/Tools/Git)
   - Edit script mentioned above and adjust BRANCH and LOCATION
   - Execute script
3. Identify changes by comparison with the previous release branch (remote->origin/blender-vX.XX-release) 
   for the change log on web page:
   - Focus comparison on source/blender/makesdna
   - Especially check source/blender/makesdna/DNA_ID.h: If new block types (IDs) have been added, 
     those have to be added manually in the source code: src/org/cakelab/blender/io/block/BlockCodes.java .
   - Do a quick scan of the changes to find major changes, which might be interesting/API breaking to 
     developers and write those down too.
   - Put your results in org.cakelab.blender.dna/doc/CHANGES.txt .
   
   

IV. Create Source Code Docs
---------------------------
Refers to script "org.cakelab.blender.dnadoc/doxygen-dnasrcdoc-xml.sh"
1. Adjust version number in script (BLENDER_VERSION)!
2. Adjust ENV_PATH_BASE in script if necessary.
3. Execute script.
	> cd <path-to>/org.cakelab.blender.dnadoc
	> ./doxygen-dnasrcdoc-xml.sh

Result: New doc file "resources/dnadoc/VERSION/dnasrc/doc.json"


V. Copy added Documentation
---------------------------
1. Go in org.cakelab.blender.dnadoc/resources/dnadoc
2. Copy folder PREVIOUS_VER/added to NEW_VER/added
3. Update version entry in doc.json (at least)
4. Review the content and compare with docs in dnasrc and pyapi


VI. Do a Test-Run with Class Generator
--------------------------------------
1. Get a .blend file of the new version (e.g. save the default file) 
2. Copy that file to org.cakelab.blender.io/versions/NEW_VERSION.blend (its temporary for now)
3. Add a temporary test project as target for generated classes
4. Adjust Eclipse run configuration or script to do a test generation run into that target project
5. Execute generator
6. Adjust versions/NEW_VERSION.blend according to version range given in output of generator
7. Validate generated output


VII. Update Java.Blend API Project
----------------------------------
1. Update the launcher or script to write generated classes to org.cakelab.blender.dna/src
2. Open a shell (!) and remove the content of org.cakelab.blender.dna/src 
   (necessary to prevent git plugin to interfere and have the file marked as removed instead of changes)
   > rm -r /home/homac/repos/git/cakelab.org/playground/org.cakelab.blender.dna/src/*
3. Run generator to generate dna classes.


VIII. Test Demo Applications
----------------------------
1. Open the example .blend files in org.cakelab.blender.viewer/examples in 
   new Blender version and save them (now converted).
   Note: If files do not open or Blender freezes -> Uncheck "Load UI" in "Open File" dialog
2. Refresh package view in IDE (so it actually sees changes)
3. Test viewer
4. Test Blender2Json converter
5. Test copy buffer exchange


IX. Create new DNA Lib
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




X. Publish
---------------
0. Upload artifacts (downloads) first and wait until finished!
1. org.cakelab.blender.io
  + update version in projexp.cfg (if necessary)
  + update doc/index.html
  + run projexp JavaBlend
  + review changes in browser file:///home/homac/tmp/public_html/projects/JavaBlend/index.html
  + upload changes (scp -r tmp/public_html/projects/JavaBlend/* cakelab.org:public_html/projects/JavaBlend/.)
2. org.cakelab.blender.viewer
  + update version in projexp.cfg and export-src.xml (if necessary)
  + update doc/index.html
  + run projexp JavaBlendViewer
  + run ant -f export-src.xml
  + upload changes (scp -r tmp/public_html/projects/JavaBlendViewer/* cakelab.org:public_html/projects/JavaBlendViewer/.)
3. org.cakelab.blender.fileviewer
  + update version in projexp.cfg and export-src.xml (if necessary)
  + update doc/index.html
  + run projexp
  + run ant -f export-src.xml
  + upload changes (scp -r tmp/public_html/projects/JavaBlendFileViewer/* cakelab.org:public_html/projects/JavaBlendFileViewer/.)
	


XI. Commit Changes
------------------

1. Commit all changes
2. Publish (mirror)
	+ org.cakelab.appbase
	+ org.cakelab.soapbox
	+ org.cakelab.jdoxml
	+ org.cakelab.json
	+ org.cakelab.blender.io
	+ org.cakelab.blender.dna
	+ org.cakelab.blender.dnadoc
	+ org.cakelab.blender.viewer
	+ org.cakelab.blender.fileviewer



