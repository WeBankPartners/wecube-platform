#!/bin/bash

if [[ -n `docker ps -a|grep -v 'platform'|grep -v 'wecube-db'|grep -v 'wecube-portal'|grep -v 'wecube-minio'|awk '{print $1}'` ]]
then
  docker rm -f `docker ps -a|grep -v 'platform'|grep -v 'wecube-db'|grep -v 'wecube-portal'|grep -v 'wecube-minio'|awk '{print $1}'`
fi
if [[ -n `docker images|grep -v 'platform'|grep -v 'wecube-db'|grep -v 'wecube-portal'|grep -v 'minio'|grep -v 'nginx'|grep -v 'mysql'|grep -v 'maven'|grep -v 'alpine'|awk '{print $1":"$2}'` ]]
then
  docker rmi `docker images|grep -v 'platform'|grep -v 'wecube-db'|grep -v 'wecube-portal'|grep -v 'minio'|grep -v 'nginx'|grep -v 'mysql'|grep -v 'maven'|grep -v 'alpine'|awk '{print $1":"$2}'`
fi
docker-compose -f docker-compose.yml down
docker-compose -f wecube_core_mysql.yml down
if [[ -n `docker images|grep -v $1|grep -E 'platform-core|platform-gateway|wecube-portal|platform-auth-server|wecube-db'|awk '{print $1":"$2}'` ]]
then
  docker rmi `docker images|grep -v $1|grep -E 'platform-core|platform-gateway|wecube-portal|platform-auth-server|wecube-db'|awk '{print $1":"$2}'`
fi
if [[ -n `docker images|grep none|awk '{print $3}'` ]]
then
  docker rmi `docker images|grep none|awk '{print $3}'`
fi