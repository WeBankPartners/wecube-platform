#!/bin/bash

VERSION=$1
if [ -z $VERSION ];then
    VERSION="dev"
fi
cd `dirname $0`

set -ex

mkdir -p database

echo "SET NAMES utf8;" > ./database/000000_create_database.sql
echo "create database wecube charset = utf8;" >> ./database/000000_create_database.sql
echo "use wecube;" >> ./database/000000_create_database.sql
for i in `ls -1 ../../platform-core/wiki/database/*.sql`; do
     CONTENTS=`cat $i`
     echo $CONTENTS >> ./database/000000_create_database.sql
done

echo "SET NAMES utf8;" > ./database/000001_create_database.sql
echo "create database auth charset = utf8;" >> ./database/000001_create_database.sql
echo "use auth;" >> ./database/000001_create_database.sql
for i in `ls -1 ../../platform-auth-server/deploy/database/*.sql`; do
     CONTENTS=`cat $i`
     echo $CONTENTS >> ./database/000001_create_database.sql
done
for i in `ls -1 ../../platform-auth-server/deploy/db/upgrade/*.sql`; do
     CONTENTS=`cat $i`
     echo $CONTENTS >> ./database/000001_create_database.sql
done

docker build -t wecube-db:$VERSION .
rm -rf database
