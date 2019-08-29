#!/bin/bash

if [ $# -ne 2 ]
  then
    echo "Usage: deploy_generate_compose.sh CONFIG IMAGE_VERSION"
    exit 1
fi

source $1

image_version=$2

build_path=`dirname $0`

sed  "s~{{WECUBE_DATABASE_IMAGE_NAME}}~$database_image_name~g" ${build_path}/docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_IMAGE_NAME}}~$wecube_image_name~g" docker-compose.yml  
sed -i "s~{{WECUBE_SERVER_PORT}}~$wecube_server_port~g" docker-compose.yml 
sed -i "s~{{MYSQL_ROOT_PASSWORD}}~$database_init_password~g" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_url~g" docker-compose.yml 
sed -i "s~{{CMDB_SERVER_URL}}~$cmdb_url~g" docker-compose.yml 
sed -i "s~{{WECUBE_SERVER_IP}}~$wecube_server_ip~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOSTS}}~$wecube_plugin_hosts~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PORT}}~$wecube_plugin_host_port~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_USER}}~$wecube_plugin_host_user~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PWD}}~$wecube_plugin_host_pwd~g" docker-compose.yml
sed -i "s~{{S3_URL}}~$s3_url~g" docker-compose.yml
sed -i "s~{{S3_ACCESS_KEY}}~$s3_access_key~g" docker-compose.yml
sed -i "s~{{S3_SECRET_KEY}}~$s3_secret_key~g" docker-compose.yml
sed -i "s~{{IMAGE_VERSION}}~$image_version~g" docker-compose.yml
