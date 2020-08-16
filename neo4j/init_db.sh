#!/bin/bash
rm -rf data_backup/last_init_backup
mkdir data_backup/last_init_backup
mv data/databases data_backup/last_init_backup
mv data/dbms data_backup/last_init_backup
cp -r init_data/* data
