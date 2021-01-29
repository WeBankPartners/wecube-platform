#!/bin/sh
mkdir -p /data/auth_server/log
java -jar /application/platform-auth-server.jar  --server.address=0.0.0.0 --server.port=8080 \
--platform.auth.server.jwt-token.user-access-token=${USER_ACCESS_TOKEN} \
--platform.auth.server.jwt-token.user-refresh-token=${USER_REFRESH_TOKEN} \
${AUTH_CUSTOM_PARAM} \
2>&1 >>/data/auth_server/log/platform-auth-server.log 
