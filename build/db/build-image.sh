#!/bin/bash

VERSION=$1
if [ -z $VERSION ];then
    VERSION="dev"
fi
cd `dirname $0`

set -ex

mkdir -p database

PLATFORMCORE_SQL=./database/000000_platformcore_create_database.sql
AUTHSERVER_SQL=./database/000001_authserver_create_database.sql

echo "SET NAMES utf8;" > $PLATFORMCORE_SQL
echo "create database wecube charset = utf8;" >> $PLATFORMCORE_SQL
echo "use wecube;" >> $PLATFORMCORE_SQL
for i in `ls -1 ../../platform-core/wiki/database/*.sql`; do
     cat $i >> $PLATFORMCORE_SQL
done

echo "SET NAMES utf8;" > $AUTHSERVER_SQL
echo "create database auth_server charset = utf8;" >> $AUTHSERVER_SQL
echo "use auth_server;" >> $AUTHSERVER_SQL
for i in `ls -1 ../../platform-auth-server/deploy/database/*.sql`; do
     cat $i >> $AUTHSERVER_SQL
done
for i in `ls -1 ../../platform-auth-server/deploy/db/upgrade/*.sql`; do
     cat $i >> $AUTHSERVER_SQL
done

docker build -t wecube-db:$VERSION .
rm -rf database
