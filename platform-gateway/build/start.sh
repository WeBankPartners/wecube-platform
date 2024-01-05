#!/bin/bash

sed -i "s~\[#server_port\]~$server_port~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#log_level\]~$log_level~g" /app/platform-auth-server/config/default.json
sed -i "s~\[#route_config_address\]~$log_level~g" /app/platform-auth-server/config/default.json

exec ./platform-auth-server