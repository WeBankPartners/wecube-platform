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

source wecube-core.cfg

sed  "s~{{WECUBE_DATABASE_IMAGE_NAME}}~$database_image_name~" docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_CORE_IMAGE_NAME}}~$wecube_image_name~" docker-compose.yml  
sed -i "s~{{WECUBE_CORE_EXTERNAL_PORT}}~$wecube_core_external_port~" docker-compose.yml 
sed -i "s~{{MYSQL_USER_PASSWORD}}~$database_user_password~" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_url~" docker-compose.yml 
sed -i "s~{{CMDB_SERVER_URL}}~$cmdb_url~" docker-compose.yml 
sed -i "s~{{WECUBE_CORE_EXTERNAL_IP}}~$wecube_core_exteranl_ip~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOSTS}}~$wecube_plugin_hosts~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PORT}}~$wecube_plugin_host_port~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_USER}}~$wecube_plugin_host_user~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PWD}}~$wecube_plugin_host_pwd~" docker-compose.yml
sed -i "s~{{S3_ENDPOINT}}~$s3_endpoint~" docker-compose.yml
sed -i "s~{{S3_ACCESS_KEY}}~$s3_access_key~" docker-compose.yml
sed -i "s~{{S3_SECRET_KEY}}~$s3_secret_key~" docker-compose.yml



docker-compose  -f docker-compose.yml  up -d











