#!/bin/bash

if [ $# -ne 2 ]
  then
    echo "Usage: deploy_generate_compose.sh CONFIG VERSION "
    exit 1
fi

source $1

wecube_image_version=$2
portal_image_version=$2
gateway_image_version=$2
auth_server_image_version=$2

build_path=$(dirname $0)

sed  "s~{{WECUBE_IMAGE_NAME}}~$wecube_image_name~g" ${build_path}/docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_SERVER_PORT}}~$wecube_server_port~g" docker-compose.yml 
sed -i "s~{{MYSQL_USER_PASSWORD}}~$mysql_user_password~g" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_server_url~g" docker-compose.yml 
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
sed -i "s~{{STATIC_RESOURCE_SERVER_IP}}~$static_resource_server_ip~g" docker-compose.yml
sed -i "s~{{STATIC_RESOURCE_SERVER_USER}}~$static_resource_server_user~g" docker-compose.yml
sed -i "s~{{STATIC_RESOURCE_SERVER_PASSWORD}}~$static_resource_server_password~g" docker-compose.yml
sed -i "s~{{STATIC_RESOURCE_SERVER_PORT}}~$static_resource_server_port~g" docker-compose.yml
sed -i "s~{{STATIC_RESOURCE_SERVER_PATH}}~$static_resource_server_path~g" docker-compose.yml
sed -i "s~{{GATEWAY_URL}}~$gateway_url~g" docker-compose.yml
sed -i "s~{{GATEWAY_CUSTOM_PARAM}}~$gateway_custom_param~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_IMAGE_NAME}}~$auth_server_image_name~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_IMAGE_VERSION}}~$auth_server_image_version~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_MYSQL_ADDR}}~$auth_server_mysql_addr~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_MYSQL_PORT}}~$auth_server_mysql_port~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_DATABASE_NAME}}~$auth_server_database_name~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_MYSQL_USER_NAME}}~$auth_server_mysql_user_name~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_MYSQL_USER_PASSWORD}}~$auth_server_mysql_user_password~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_PORT}}~$auth_server_port~g" docker-compose.yml
sed -i "s~{{AUTH_CUSTOM_PARAM}}~$auth_custom_param~g" docker-compose.yml
sed -i "s~{{PUBLIC_DOMAIN}}~$public_domain~g" docker-compose.yml
sed -i "s~{{JWT_SSO_AUTH_URI}}~$jwt_sso_auth_uri~g" docker-compose.yml
sed -i "s~{{JWT_SSO_TOKEN_URI}}~$jwt_sso_token_uri~g" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_DEPLOY_PATH}}~$wecube_plugin_deploy_path~g" docker-compose.yml
sed -i "s~{{WECUBE_CUSTOM_PARAM}}~$wecube_custom_param~g" docker-compose.yml
sed -i "s~{{WECUBE_CORE_HOST}}~$wecube_core_host~g" docker-compose.yml
sed -i "s~{{AUTH_SERVER_HOST}}~$auth_server_host~g" docker-compose.yml
sed -i "s~{{WECUBE_SERVER_JMX_PORT}}~$wecube_server_jmx_port~g" docker-compose.yml
sed -i "s~{{WECUBE_BUCKET}}~$wecube_bucket~g" docker-compose.yml
sed -i "s~{{DOCKER_API_CERTS_PATH}}~$docker_api_certs_path~g" docker-compose.yml
sed -i "s~{{APP_LOG_PATH}}~$app_log_path~g" docker-compose.yml
