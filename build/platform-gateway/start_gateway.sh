#!/bin/sh
mkdir -p /log
java -jar /application/platform-gateway.jar  --server.address=0.0.0.0 --server.port=8080 \
--platform.gateway.route.route-config-server=${GATEWAY_ROUTE_CONFIG_SERVER} \
--platform.gateway.route.route-config-uri=${GATEWAY_ROUTE_CONFIG_URI} \
--platform.gateway.route.route-config-access-key=${GATEWAY_ROUTE_ACCESS_KEY} \
--spring.cloud.gateway.routes.id=platform-core \
--spring.cloud.gateway.routes.uri=${GATEWAY_ROUTES_PLATFORM_CORE_URI} \
2>&1 >>/log/wecube-gateway.log 
