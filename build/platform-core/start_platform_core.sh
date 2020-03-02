#!/bin/sh
mkdir -p /log
java -Djava.security.egd=file:/dev/urandom -jar /application/platform-core.jar  --server.address=0.0.0.0 --server.port=8080 \
--spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver \
--spring.datasource.url=jdbc:mysql://${MYSQL_SERVER_ADDR}:${MYSQL_SERVER_PORT}/${MYSQL_SERVER_DATABASE_NAME}?serverTimezone=Asia\/Shanghai\&characterEncoding=utf8 \
--spring.datasource.username=${MYSQL_USER_NAME} \
--spring.datasource.password=${MYSQL_USER_PASSWORD}  \
--wecube.core.plugin.plugin-hosts=${WECUBE_PLUGIN_HOSTS} \
--wecube.core.plugin.default-host-ssh-user=${WECUBE_PLUGIN_HOST_USER} \
--wecube.core.plugin.default-host-ssh-port=${WECUBE_PLUGIN_HOST_PORT} \
--wecube.core.plugin.default-host-ssh-password=${WECUBE_PLUGIN_HOST_PWD} \
--wecube.core.s3.endpoint=${S3_ENDPOINT} \
--wecube.core.s3.access-key=${S3_ACCESS_KEY} \
--wecube.core.s3.secret-key=${S3_SECRET_KEY} \
--wecube.core.cmdb-data.enum-category-ci-type-layer=ci_layer \
--wecube.core.cmdb-data.enum-category-ci-type-catalog=ci_catalog \
--wecube.core.gateway-url=${GATEWAY_URL} \
--wecube.core.plugin.plugin-package-name-of-deploy=salt-stack-deployment \
--wecube.core.cmdb-data.enum-category-ci-type-zoom-levels=ci_zoom_level \
--wecube.core.plugin.static-resource-server-ip=${STATIC_RESOURCE_SERVER_IP} \
--wecube.core.plugin.static-resource-server-user=${STATIC_RESOURCE_SERVER_USER} \
--wecube.core.plugin.static-resource-server-password=${STATIC_RESOURCE_SERVER_PASSWORD} \
--wecube.core.plugin.static-resource-server-port=${STATIC_RESOURCE_SERVER_PORT} \
--wecube.core.plugin.static-resource-server-path=${STATIC_RESOURCE_SERVER_PATH} \
--platform.auth.jwt-sso-authentication-uri=${JWT_SSO_AUTH_URI} \
--platform.auth.jwt-sso-access-token-uri=${JWT_SSO_TOKEN_URI} \
--wecube.core.authserver.host=${GATEWAY_HOST} \
--wecube.core.authserver.port=${GATEWAY_PORT} \
--wecube.core.plugin.plugin-deploy-path=${WECUBE_PLUGIN_DEPLOY_PATH} \
--wecube.core.plugin.plugin-package-bucket-name=${WECUBE_BUCKET} \
${WECUBE_CUSTOM_PARAM} \
>>/log/wecube-core.log 
