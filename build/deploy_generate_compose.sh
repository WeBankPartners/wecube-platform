#!/bin/bash

if [ $# -ne 4 ]
  then
    echo "Usage: deploy_generate_compose.sh CONFIG PLIATFORM_CORE_IMAGE_VERSION PORTAL_IMAGE_VERSION GATEWAY_IMAGE_VERSION"
    exit 1
fi

source $1

wecube_image_version=$2
portal_image_version=$3
gateway_image_version=$4

build_path=$(dirname $0)

sed  "s~{{WECUBE_IMAGE_NAME}}~$wecube_image_name~g" ${build_path}/docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_SERVER_PORT}}~$wecube_server_port~g" docker-compose.yml 
sed -i "s~{{MYSQL_ROOT_PASSWORD}}~$mysql_root_password~g" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_server_url~g" docker-compose.yml 
sed -i "s~{{CMDB_SERVER_URL}}~$cmdb_server_url~g" docker-compose.yml 
sed -i "s~{{WECUBE_SERVER_IP}}~$wecube_server_ip~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOSTS}}~$wecube_plugin_hosts~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PORT}}~$wecube_plugin_host_port~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_USER}}~$wecube_plugin_host_user~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PWD}}~$wecube_plugin_host_pwd~g" docker-compose.yml
sed -i "s~{{S3_URL}}~$s3_url~g" docker-compose.yml
sed -i "s~{{S3_ACCESS_KEY}}~$s3_access_key~g" docker-compose.yml
sed -i "s~{{S3_SECRET_KEY}}~$s3_secret_key~g" docker-compose.yml
sed -i "s~{{WECUBE_IMAGE_VERSION}}~$wecube_image_version~g" docker-compose.yml
sed -i "s~{{MYSQL_SERVER_ADDR}}~$mysql_server_addr~g" docker-compose.yml
sed -i "s~{{MYSQL_SERVER_PORT}}~$mysql_server_port~g" docker-compose.yml
sed -i "s~{{MYSQL_SERVER_DATABASE_NAME}}~$mysql_server_database_name~g" docker-compose.yml
sed -i "s~{{MYSQL_USER_NAME}}~$mysql_user_name~g" docker-compose.yml
sed -i "s~{{PORTAL_IMAGE}}~$portal_image~g" docker-compose.yml
sed -i "s~{{PORTAL_PORT}}~$portal_port~g" docker-compose.yml
sed -i "s~{{PORTAL_IMAGE_VERSION}}~$portal_image_version~g" docker-compose.yml
sed -i "s~{{GATEWAY_HOST}}~$gateway_host~g" docker-compose.yml
sed -i "s~{{GATEWAY_PORT}}~$gateway_port~g" docker-compose.yml
sed -i "s~{{GATEWAY_IMAGE_NAME}}~$gateway_image_name~g" docker-compose.yml
sed -i "s~{{GATEWAY_IMAGE_VERSION}}~$gateway_image_version~g" docker-compose.yml
sed -i "s~{{GATEWAY_ROUTE_CONFIG_SERVER}}~$gateway_route_config_server~g" docker-compose.yml
sed -i "s~{{GATEWAY_ROUTE_CONFIG_URI}}~$gateway_route_config_uri~g" docker-compose.yml
sed -i "s~{{GATEWAY_ROUTE_ACCESS_KEY}}~$gateway_route_access_key~g" docker-compose.yml
sed -i "s~{{GATEWAY_ROUTES_PLATFORM_CORE_URI}}~$gateway_routes_platform_core_uri~g" docker-compose.yml
