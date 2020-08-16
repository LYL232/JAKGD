#!/bin/bash
time=$(date "+%Y%m%d%H%M%S")
mkdir data_backup/$time
cp -r data/databases data_backup/$time
cp -r data/dbms data_backup/$time
