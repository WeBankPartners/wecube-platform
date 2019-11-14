#!/bin/sh
mkdir -p /log
java -jar /application/platform-gateway.jar  --server.address=0.0.0.0 --server.port=8080 \
--platform.gateway.route.route-config-server=${GATEWAY_ROUTE_CONFIG_SERVER} \
--platform.gateway.route.route-config-uri=${GATEWAY_ROUTE_CONFIG_URI} \
--platform.gateway.route.route-config-access-key=${GATEWAY_ROUTE_ACCESS_KEY} \
--spring.cloud.gateway.routes[0].id=platform-core \
--spring.cloud.gateway.routes[0].uri=${GATEWAY_ROUTES_PLATFORM_CORE_URI} \
--spring.cloud.gateway.routes[0].predicates[0].Path=${GATEWAY_ROUTES_PLATFORM_CORE_PREDICATE_PATH} \
--spring.cloud.gateway.routes[0].filters[0].DynamicRoute=${GATEWAY_ROUTES_PLATFORM_CORE_FILTERS_DYNROUTE} \
2>&1 >>/log/wecube-gateway.log 
