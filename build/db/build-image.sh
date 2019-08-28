#!/bin/bash
set -ex
cp -r ../../wecube-core/src/main/resources/database  database

TEXT='use wecube;'

cd database
for i in `ls -1 ./*.sql`; do
     CONTENTS=`cat $i`
     echo $TEXT > $i  
     echo $CONTENTS >> $i
done
cd ../

echo "SET NAMES utf8;" > ./database/000000_create_database.sql
echo "create database wecube charset = utf8;" >> ./database/000000_create_database.sql

docker build -t wecube-db:dev .
rm -rf database
