# FixMyBug
CS Comps

Changelog
10/20/16 - Added setup notes - Joe

SETUP NOTES

The database lives in the root directory of the computer (in "/FixMyBugDB"), so everyone should have access to it. Trying to open an sqlite database that doesn't exist will create a new database, so there are a few TEST_DATABASEs running around; they are empty, and if you find one (especially in the git repo), go ahead and delete it.

Also in the root directory is the Maven archive. To get that up and running, all you need to do is edit your ~/.bash_profile folder and add the path "/FixMyBugDB/apache-maven-3.3.9/bin" to the PATH environment variable. You can verify that maven is installed by typing "mvn -v" and looking for output.

To "compile" the client or server programs, navigate to the correct folder (e.g. gs-rest-server) and type "mvn package". This should create a jar that you can then execute.
