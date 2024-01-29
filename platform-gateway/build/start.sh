#!/bin/bash

sed -i "s~\[#LOG_LEVEL\]~$LOG_LEVEL~g" /app/platform-gateway/config/default.json
sed -i "s~\[#GATEWAY_ROUTE_CONFIG_SERVER\]~$GATEWAY_ROUTE_CONFIG_SERVER~g" /app/platform-gateway/config/default.json
sed -i "s~\[#GATEWAY_ROUTE_CONFIG_URI\]~$GATEWAY_ROUTE_CONFIG_URI~g" /app/platform-gateway/config/default.json
sed -i "s~\[#WECUBE_CORE_ADDRESS\]~$WECUBE_CORE_ADDRESS~g" /app/platform-gateway/config/default.json
sed -i "s~\[#AUTH_SERVER_ADDRESS\]~$AUTH_SERVER_ADDRESS~g" /app/platform-gateway/config/default.json

exec ./platform-gateway