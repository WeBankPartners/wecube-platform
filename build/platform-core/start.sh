#!/bin/bash

sed -i "s~{{version}}~$version~g" /app/platform-core/config/default.json
sed -i "s~{{http_port}}~$http_port~g" /app/platform-core/config/default.json
sed -i "s~{{log_level}}~$log_level~g" /app/platform-core/config/default.json
sed -i "s~{{db_server}}~$db_server~g" /app/platform-core/config/default.json
sed -i "s~{{db_port}}~$db_port~g" /app/platform-core/config/default.json
sed -i "s~{{db_user}}~$db_user~g" /app/platform-core/config/default.json
sed -i "s~{{db_pass}}~$db_pass~g" /app/platform-core/config/default.json
sed -i "s~{{db_database}}~$db_database~g" /app/platform-core/config/default.json

exec ./platform-core

