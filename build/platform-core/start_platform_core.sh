#!/bin/sh
mkdir -p /data/wecube/log

if [ 'true' == "$WECUBE_SERVER_JMX" ]; then 
    JMX_ARGS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=${WECUBE_SERVER_JMX_PORT} -Dcom.sun.management.jmxremote.rmi.port=${WECUBE_SERVER_JMX_PORT} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
else 
    JMX_ARGS=""
fi

java ${JMX_ARGS} -Djava.rmi.server.hostname=${WECUBE_CORE_HOST} -Djava.security.egd=file:/dev/urandom \
-jar /application/platform-core.jar  --server.address=0.0.0.0 --server.port=8080 \
--wecube.core.s3.endpoint=${S3_ENDPOINT} \
--wecube.core.s3.access-key=${S3_ACCESS_KEY} \
--wecube.core.s3.secret-key=${S3_SECRET_KEY} \
--wecube.core.gateway-url=${GATEWAY_URL} \
--wecube.core.gateway-host-ports=${GATEWAY_HOST_PORTS} \
--platform.auth.jwt-sso-authentication-uri=${JWT_SSO_AUTH_URI} \
--platform.auth.jwt-sso-access-token-uri=${JWT_SSO_TOKEN_URI} \
--wecube.core.authserver.host=${GATEWAY_HOST} \
--wecube.core.authserver.port=${GATEWAY_PORT} \
--wecube.core.plugin.plugin-deploy-path=${WECUBE_PLUGIN_DEPLOY_PATH} \
--wecube.core.plugin.plugin-package-bucket-name=${WECUBE_BUCKET} \
${WECUBE_CUSTOM_PARAM} \
>>/data/wecube/log/wecube-core.log 
