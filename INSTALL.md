This is a Maven project.  To build it with maven run 'mvn clean
install'.  It is also an Eclipse project and can be built from within
Eclipse.  An easy way to generate the command-line utility is to export it as an
runnable jar file from within Eclipse.

To create a runnable jar file, clone the git repo and import it into
Eclipse as a Maven project.  Then after building it in Eclipse, export
it as a Java runnable jar named removeGerritProj.jar, for instance.
The jar file can be executed from the command-line using 'java -jar
removeGerritProj.jar'.

So far, this utililty has only been tested against MySQL.  A JDBC
driver is needed.  Just use whichever JDBC driver Gerrit uses.  If
Gerrit is installed in /some/path/to/gerrit, then the driver will be
in /some/path/to/gerrit/lib.  The MySQL JDBC driver is named
mysql-connector-java-5.1.xx.jar.

