TwitterCrawler
==============

A distributed Twitter data crawler

This crawler is designed to run on a set of machines which have a Samba program to
synchronize the file storage.

Now the crawler strategy is a BFS style.  Start from a root user(me...) follow the
following list to recursively crawl other users.  Now I only keep user profile, most
recent 100 tweets, the user's friends, followers and following list.

Run
=============

To depoly the crawler:

0. fill your Twitter App key, token in twitter4j.properties

1. ant

2. generate data folder
cd data; sh mkdir-script.sh

3. sh ./jobAss.sh to generate job assignment for each machine.  Now it's 3 by default.
TODO:: move the number of machine in properties file.

4. sh ./command.sh to start the crawler.

5. sh ./printRate.sh could show current API usage.

Multi-machine setup
============

Finished 
TODO:: add description about it.

Data
=============

The data folder stores all crawled information in json format.  Each sub directory stores
the users whose name starts the that letter.

./data/friend
stores users' friend

./data/follower
stores users' follower

.data/friendList
stores users' friend list

./data/tweet
stores users' most recent 100 tweets

./data/profile
stores users' profile