#!/bin/sh
mkdir -p /log
java -jar /application/platform-auth-server.jar  --server.address=0.0.0.0 --server.port=8080 \
--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
--spring.datasource.url=jdbc:mysql://${AUTH_SERVER_MYSQL_ADDR}:${AUTH_SERVER_MYSQL_PORT}/${AUTH_SERVER_DATABASE_NAME}?serverTimezone=Asia\/Shanghai\&characterEncoding=utf8 \
--spring.datasource.username=${AUTH_SERVER_MYSQL_USER_NAME} \
--spring.datasource.password=${AUTH_SERVER_MYSQL_USER_PASSWORD}  \
2>&1 >>/log/platform-auth-server.log 
