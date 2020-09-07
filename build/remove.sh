#!/bin/bash

docker-compose -f docker-compose.yml down
docker-compose -f wecube_core_mysql.yml down
if [[ -n `docker images|grep -v $1|grep -E 'platform-core|platform-gateway|wecube-portal|platform-auth-server|wecube-db'|awk '{print $1":"$2}'` ]]
then
  docker rmi `docker images|grep -v $1|grep -E 'platform-core|platform-gateway|wecube-portal|platform-auth-server|wecube-db'|awk '{print $1":"$2}'`
fi