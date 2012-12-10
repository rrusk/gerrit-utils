gerrit-utils
============

Some administrative utility routines for the Gerrit code-review system that support features that cannot be
accomplished easily from within Gerrit itself.

These utilities have been tested with Gerrit 2.5-rc1 and 2.5 final configured with a MySQL database.
They must be ran from the account used by Gerrit.

The utililty "project-remove" will first remove the Gerrit git repo for the specified project and
then it will remove references to that project from the Gerrit database.

TODOS:
1. add support for PostGreSQL databases
2. support renaming projects
3. add a check for database version
4. add test code
