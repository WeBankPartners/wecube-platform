#!/bin/bash

sed -i "s~\[#server_port\]~$server_port~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#log_level\]~$log_level~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#db_server\]~$db_server~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#db_port\]~$db_port~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#db_user\]~$db_user~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#db_pass\]~$db_pass~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#db_database\]~$db_database~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#auth_access_token_mins\]~$auth_access_token_mins~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#auth_refresh_token_hours\]~$auth_refresh_token_hours~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#um_address\]~$um_address~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#um_app_id\]~$um_app_id~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#um_app_token\]~$um_app_token~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#jwt_private_key_path\]~$jwt_private_key_path~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#jwt_public_key_path\]~$jwt_public_key_path~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#um_permission_system_id\]~$um_permission_system_id~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#um_permission_address\]~$um_permission_address~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#um_permission_cron_express\]~$um_permission_cron_express~g" /app/platform-auth-server/config/default.json

exec ./platform-auth-server