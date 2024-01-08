#!/bin/bash

sed -i "s~\[#LOG_LEVEL\]~$LOG_LEVEL~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#PASSWORD_PRIVATE_KEY_PATH\]~$PASSWORD_PRIVATE_KEY_PATH~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#MYSQL_SERVER_ADDR\]~$MYSQL_SERVER_ADDR~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#MYSQL_SERVER_PORT\]~$MYSQL_SERVER_PORT~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#MYSQL_USER_NAME\]~$MYSQL_USER_NAME~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#MYSQL_USER_PASSWORD\]~$MYSQL_USER_PASSWORD~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#MYSQL_SERVER_DATABASE_NAME\]~$MYSQL_SERVER_DATABASE_NAME~g" /app/platform-auth-server/config/default.json

sed -i "s~\[#JWT_PRI_KEY_PATH\]~$JWT_PRI_KEY_PATH~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#JWT_PUB_KEY_PATH\]~$JWT_PUB_KEY_PATH~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#USER_ACCESS_TOKEN\]~$USER_ACCESS_TOKEN~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#USER_REFRESH_TOKEN\]~$USER_REFRESH_TOKEN~g" /app/platform-auth-server/config/default.json

exec ./platform-auth-server