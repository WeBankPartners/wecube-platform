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
      - WECUBE_GATEWAY_LOG_PATH=[#WECUBE_GATEWAY_LOG_PATH]
      - GATEWAY_ROUTE_CONFIG_SERVER=[#GATEWAY_ROUTE_CONFIG_SERVER]
      - GATEWAY_ROUTE_CONFIG_URI=[#GATEWAY_ROUTE_CONFIG_URI]
      - WECUBE_CORE_HOST=[#WECUBE_CORE_HOST]
      - AUTH_SERVER_HOST=[#AUTH_SERVER_HOST]
      - AUTH_SERVER_LOG_PATH=[#AUTH_SERVER_LOG_PATH]
