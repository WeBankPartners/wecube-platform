version: '3'
services:
  platform-gateway:
    image: platform-gateway:{{version}}
    container_name: platform-gateway-{{version}}
    restart: always
    volumes:
      - /etc/localtime:/etc/localtime
      - [#path]/platform-gateway/logs:/app/platform-gateway/logs
    ports:
      - "[@HOSTIP]:[#http_port]:8080"
    environment:
      - LOG_LEVEL=[#LOG_LEVEL]
      - GATEWAY_ROUTE_CONFIG_SERVER=[#GATEWAY_ROUTE_CONFIG_SERVER]
      - GATEWAY_ROUTE_CONFIG_URI=[#GATEWAY_ROUTE_CONFIG_URI]
      - WECUBE_CORE_ADDRESS=[#WECUBE_CORE_ADDRESS]
      - AUTH_SERVER_ADDRESS=[#AUTH_SERVER_ADDRESS]
