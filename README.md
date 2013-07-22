TwitterCrawler
==============

A distributed Twitter data crawler

This crawler is designed to run on a set of machines which have a Samba program to
synchronize the file storage.

Run
=============
To depoly the crawler:

0. fill your Twitter App key, token in twitter4j.properties

1. ant

2. sh ./jobAss.sh to generate job assignment for each machine.  Now it's 3 by default.
TODO:: move the number of machine in properties file.

3. sh ./command.sh to start the crawler.

4. sh ./printRate.sh could show current API usage.