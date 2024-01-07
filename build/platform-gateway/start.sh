#!/bin/bash

sed -i "s~\[#LOG_LEVEL\]~$LOG_LEVEL~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#WECUBE_GATEWAY_LOG_PATH\]~$WECUBE_GATEWAY_LOG_PATH~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#GATEWAY_ROUTE_CONFIG_SERVER\]~$GATEWAY_ROUTE_CONFIG_SERVER~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#GATEWAY_ROUTE_CONFIG_URI\]~$GATEWAY_ROUTE_CONFIG_URI~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#WECUBE_CORE_HOST\]~$WECUBE_CORE_HOST~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#AUTH_SERVER_HOST\]~$AUTH_SERVER_HOST~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#AUTH_SERVER_LOG_PATH\]~$AUTH_SERVER_LOG_PATH~g" /app/platform-auth-server/config/default.json

exec ./platform-auth-server