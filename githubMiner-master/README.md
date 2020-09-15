# Description

Files to run: downloadRunner.py, finderRunner.py (no command line parameters needed)

## Set Parameters

All necessary parameters should be given in Crawler/config.py.

## Issues

* if db schema in DB.txt gives errors, use the following

  ```SQL
  CREATE TABLE `repos` (
    `id` int(20) NOT NULL,
    `url` varchar(200) NOT NULL,
    `language` varchar(20) DEFAULT NULL,
    `downloaded` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `repos_id_uindex` (`id`),
    UNIQUE KEY `repos_url_uindex` (`url`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8

  ```

* install github and sql connector module by this command:
   1. pip install pygithub
   2. pip install mysql-connector
