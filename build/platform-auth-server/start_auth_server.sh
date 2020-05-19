#!/bin/sh
mkdir -p /log
java -jar /application/platform-auth-server.jar  --server.address=0.0.0.0 --server.port=8080 \
--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
--spring.datasource.url=jdbc:mysql://${MYSQL_SERVER_ADDR}:${MYSQL_SERVER_PORT}/${MYSQL_SERVER_DATABASE_NAME}?serverTimezone=Asia\/Shanghai\&characterEncoding=utf8 \
--spring.datasource.username=${MYSQL_USER_NAME} \
--spring.datasource.password=${MYSQL_USER_PASSWORD}  \
--platform.auth.server.jwt-token.user-access-token=${USER_ACCESS_TOKEN} \
--platform.auth.server.jwt-token.user-refresh-token=${USER_REFRESH_TOKEN} \
${AUTH_CUSTOM_PARAM} \
2>&1 >>/log/platform-auth-server.log 
