#!/bin/bash

sed -i "s~{{version}}~$version~g" /app/platform-core/config/default.json
sed -i "s~{{password_private_key_path}}~$password_private_key_path~g" /app/platform-core/config/default.json
sed -i "s~{{https_enable}}~$https_enable~g" /app/platform-core/config/default.json
sed -i "s~{{http_port}}~$http_port~g" /app/platform-core/config/default.json
sed -i "s~{{log_level}}~$log_level~g" /app/platform-core/config/default.json
sed -i "s~{{db_server}}~$db_server~g" /app/platform-core/config/default.json
sed -i "s~{{db_port}}~$db_port~g" /app/platform-core/config/default.json
sed -i "s~{{db_user}}~$db_user~g" /app/platform-core/config/default.json
sed -i "s~{{db_pass}}~$db_pass~g" /app/platform-core/config/default.json
sed -i "s~{{db_database}}~$db_database~g" /app/platform-core/config/default.json
sed -i "s~{{auth_server_url}}~$auth_server_url~g" /app/platform-core/config/default.json
sed -i "s~{{access_token_expired_sec}}~$access_token_expired_sec~g" /app/platform-core/config/default.json
sed -i "s~{{refresh_token_expired_sec}}~$refresh_token_expired_sec~g" /app/platform-core/config/default.json
sed -i "s~{{s3_address}}~$s3_address~g" /app/platform-core/config/default.json
sed -i "s~{{s3_access_key}}~$s3_access_key~g" /app/platform-core/config/default.json
sed -i "s~{{s3_secret_key}}~$s3_secret_key~g" /app/platform-core/config/default.json
sed -i "s~{{static_resource_server_ips}}~$static_resource_server_ips~g" /app/platform-core/config/default.json
sed -i "s~{{static_resource_server_user}}~$static_resource_server_user~g" /app/platform-core/config/default.json
sed -i "s~{{static_resource_server_password}}~$static_resource_server_password~g" /app/platform-core/config/default.json
sed -i "s~{{static_resource_server_port}}~$static_resource_server_port~g" /app/platform-core/config/default.json
sed -i "s~{{static_resource_server_path}}~$static_resource_server_path~g" /app/platform-core/config/default.json
sed -i "s~{{plugin_base_mount_path}}~$plugin_base_mount_path~g" /app/platform-core/config/default.json
sed -i "s~{{plugin_deploy_path}}~$plugin_deploy_path~g" /app/platform-core/config/default.json
sed -i "s~{{plugin_password_pub_key_path}}~$plugin_password_pub_key_path~g" /app/platform-core/config/default.json
sed -i "s~{{resource_server_password_seed}}~$resource_server_password_seed~g" /app/platform-core/config/default.json
sed -i "s~{{gateway_url}}~$gateway_url~g" /app/platform-core/config/default.json
sed -i "s~{{gateway_host_ports}}~$gateway_host_ports~g" /app/platform-core/config/default.json
sed -i "s~{{sub_system_private_key}}~$sub_system_private_key~g" /app/platform-core/config/default.json
sed -i "s~{{cron_keep_batch_exec_days}}~$cron_keep_batch_exec_days~g" /app/platform-core/config/default.json

exec ./platform-core

