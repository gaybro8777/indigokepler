# indigokepler
Thi contains actors for Indigo project

You need to build indigoclient before you can proceed with building indigokepler project.

Make sure to take a look at:

https://github.com/indigo-dc/indigoclient

After you have built and published jars from indigoclient, you can build indigokepler

	mvn initialize  # this command will install ptolemy-lepler-2.5.jar in your local repository
	mvn install     # this command will build Kepler actors. Before proceeding, make sure you have build indigoclient (https://github.com/indigo-dc/indigoclient)
	mvn dependency:copy-dependencies -DoutputDirectory=target/indigo/lib/jar -DexcludeArtifactIds=ptolemy # installs all dependencies apart ptolemy

After building indigo module, directory structure should have following layout

    indigo/
    |-- lib
    |   |-- exe/
    |   `-- jar/
    |-- lib64/
    |-- module-info/
    |-- resources
    |   `-- system.properties/
    `-- src
        `-- pl
            `-- psnc
                `-- indigo
                    `-- fg
                        `-- kepler/

Copy whole directory into $KEPLER/ where $KEPLER points to your Kepler installation

Then, make sure to add "indigo" module into (simply add name of module into list of other modules) 

        kepler/build-area
        | 
        `-- modules.txt
    
* You can now compile and run Kepler 2.5

        cd kepler/build-area
        ant compile
        ant run

# ptolemy license

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2010 The Regents of the University of California. All rights reserved.

Permission is hereby granted, without written agreement and without license or royalty fees, to use, copy, modify, and distribute this software and its documentation for any purpose, provided that the above copyright notice and the following two paragraphs appear in all copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
