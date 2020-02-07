#!/bin/bash

VERSION=$1
if [ -z $VERSION ];then
    VERSION="dev"
fi
cd `dirname $0`

set -ex
cp -r ../../platform-core/src/main/resources/database  database

TEXT='use wecube;'

cd database
for i in `ls -1 ./*.wecube.*.sql`; do
     CONTENTS=`cat $i`
     echo "SET NAMES utf8;" > $i
     echo $TEXT > $i  
     echo $CONTENTS >> $i
done
cd ../

echo "SET NAMES utf8;" > ./database/000000_create_database.sql
echo "create database wecube charset = utf8;" >> ./database/000000_create_database.sql

# setup auth
cp ../../platform-auth-server/src/main/resources/database/*  database

TEXT='use auth;'

cd database
for i in `ls -1 ./*.auth.*.sql`; do
     CONTENTS=`cat $i`
     echo "SET NAMES utf8;" > $i
     echo $TEXT > $i  
     echo $CONTENTS >> $i
done
cd ../

echo "create database auth charset = utf8;" >> ./database/000000_create_database.sql

docker build -t wecube-db:$VERSION .
rm -rf database
