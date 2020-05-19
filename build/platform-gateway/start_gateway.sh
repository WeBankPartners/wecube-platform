#!/bin/sh
mkdir -p /data/wecube-gateway/log
java -jar /application/platform-gateway.jar  --server.address=0.0.0.0 --server.port=8080 \
--platform.gateway.route.route-config-server=${GATEWAY_ROUTE_CONFIG_SERVER} \
--platform.gateway.route.route-config-uri=${GATEWAY_ROUTE_CONFIG_URI} \
--platform.gateway.route.route-config-access-key=${GATEWAY_ROUTE_ACCESS_KEY} \
${GATEWAY_CUSTOM_PARAM} \
2>&1 >>/data/wecube-gateway/log/wecube-gateway.log 
