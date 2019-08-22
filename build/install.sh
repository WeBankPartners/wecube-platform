#!/bin/bash
set -ex
if ! docker --version &> /dev/null
then
    echo "must have docker installed"
    exit 1
fi

if ! docker-compose --version &> /dev/null
then
    echo  "must have docker-compose installed"
    exit 1
fi

source wecube.cfg

sed  "s~{{WECUBE_DATABASE_IMAGE_NAME}}~$database_image_name~" docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_IMAGE_NAME}}~$wecube_image_name~" docker-compose.yml  
sed -i "s~{{WECUBE_SERVER_PORT}}~$wecube_server_port~" docker-compose.yml 
sed -i "s~{{MYSQL_ROOT_PASSWORD}}~$database_init_password~" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_url~" docker-compose.yml 
sed -i "s~{{CMDB_SERVER_URL}}~$cmdb_url~" docker-compose.yml 
sed -i "s~{{WECUBE_SERVER_IP}}~$wecube_server_ip~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOSTS}}~$wecube_plugin_hosts~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PORT}}~$wecube_plugin_host_port~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_USER}}~$wecube_plugin_host_user~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PWD}}~$wecube_plugin_host_pwd~" docker-compose.yml
sed -i "s~{{S3_URL}}~$s3_url~" docker-compose.yml
sed -i "s~{{S3_ACCESS_KEY}}~$s3_access_key~" docker-compose.yml
sed -i "s~{{S3_SECRET_KEY}}~$s3_secret_key~" docker-compose.yml

docker-compose  -f docker-compose.yml  up -d





