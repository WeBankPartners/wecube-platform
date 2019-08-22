#!/bin/bash
set -ex
cp -r ../../wecube-core/src/main/resources/database  database

TEXT='use wecube;'

cd database
for i in `ls -1 ./*`; do
     CONTENTS=`cat $i`
     echo $TEXT > $i  # use echo -n if you want the append to be on the same line
     echo $CONTENTS >> $i
done
cd ../

echo "SET NAMES utf8;" > ./database/000000_create_table.sql
echo "create database wecube charset = utf8;" >> ./database/000000_create_table.sql

docker build -t wecube-db:dev .
rm -rf database
