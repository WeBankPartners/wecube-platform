version: '2'
services:
  wecube-minio:
    image: minio/minio
    restart: always
    command: [
        'server',
        'data'
    ]
    ports:
      - 9000:9000
    volumes:
      - /data/minio-storage/data:/data    
      - /data/minio-storage/config:/root
      - /etc/localtime:/etc/localtime
    environment:
      - MINIO_ACCESS_KEY={{S3_ACCESS_KEY}}
      - MINIO_SECRET_KEY={{S3_SECRET_KEY}}
      
  wecube-portal:
    image: {{PORTAL_IMAGE}}:{{PORTAL_IMAGE_VERSION}}
    restart: always
    depends_on:
      - platform-gateway
      - platform-core
    volumes:
      - /data/wecube-portal/log:/var/log/nginx/
      - /etc/localtime:/etc/localtime
      - /data/wecube-portal/data/ui-resources:/root/app/ui-resources
    ports:
      - {{PORTAL_PORT}}:8080
    environment:
      - GATEWAY_HOST={{GATEWAY_HOST}}
      - GATEWAY_PORT={{GATEWAY_PORT}}
      - PUBLIC_DOMAIN={{PUBLIC_DOMAIN}}
      - TZ=Asia/Shanghai
    command: /bin/bash -c "/etc/nginx/start_platform_portal.sh"

  platform-gateway:
    image: {{GATEWAY_IMAGE_NAME}}:{{GATEWAY_IMAGE_VERSION}}
    restart: always
    depends_on:
      - platform-core
    volumes:
      - /data/wecube-gateway/log:{{WECUBE_GATEWAY_LOG_PATH}}
      - /etc/localtime:/etc/localtime
    ports:
      - {{GATEWAY_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - GATEWAY_ROUTE_CONFIG_SERVER={{GATEWAY_ROUTE_CONFIG_SERVER}}
      - GATEWAY_ROUTE_CONFIG_URI={{GATEWAY_ROUTE_CONFIG_URI}}
      - GATEWAY_ROUTE_ACCESS_KEY={{GATEWAY_ROUTE_ACCESS_KEY}}
      - WECUBE_CORE_HOST={{WECUBE_CORE_HOST}}
      - AUTH_SERVER_HOST={{AUTH_SERVER_HOST}}
      - GATEWAY_CUSTOM_PARAM={{GATEWAY_CUSTOM_PARAM}}
      - WECUBE_GATEWAY_LOG_PATH={{WECUBE_GATEWAY_LOG_PATH}}

  platform-core:
    image: {{WECUBE_IMAGE_NAME}}:{{WECUBE_IMAGE_VERSION}}
    restart: always
    volumes:
      - /data/wecube/log:{{APP_LOG_PATH}}
      - /etc/localtime:/etc/localtime
      - {{DOCKER_API_CERTS_PATH}}:/certs
    ports:
      - {{WECUBE_SERVER_PORT}}:8080
      - {{WECUBE_SERVER_JMX_PORT}}:{{WECUBE_SERVER_JMX_PORT}}
    environment:
      - TZ=Asia/Shanghai
      - MYSQL_SERVER_ADDR={{MYSQL_SERVER_ADDR}}
      - MYSQL_SERVER_PORT={{MYSQL_SERVER_PORT}}
      - MYSQL_SERVER_DATABASE_NAME={{MYSQL_SERVER_DATABASE_NAME}}
      - MYSQL_USER_NAME={{MYSQL_USER_NAME}}
      - MYSQL_USER_PASSWORD={{MYSQL_USER_PASSWORD}}
      - S3_ENDPOINT={{S3_URL}}
      - S3_ACCESS_KEY={{S3_ACCESS_KEY}}
      - S3_SECRET_KEY={{S3_SECRET_KEY}}
      - STATIC_RESOURCE_SERVER_IP={{STATIC_RESOURCE_SERVER_IP}}
      - STATIC_RESOURCE_SERVER_USER={{STATIC_RESOURCE_SERVER_USER}}
      - ***REMOVED***
      - STATIC_RESOURCE_SERVER_PORT={{STATIC_RESOURCE_SERVER_PORT}}
      - STATIC_RESOURCE_SERVER_PATH={{STATIC_RESOURCE_SERVER_PATH}}
      - GATEWAY_URL={{GATEWAY_URL}}
      - GATEWAY_HOST_PORTS={{GATEWAY_HOST_PORTS}}
      - GATEWAY_HOST={{GATEWAY_HOST}}
      - GATEWAY_PORT={{GATEWAY_PORT}}
      - JWT_SSO_AUTH_URI={{JWT_SSO_AUTH_URI}}
      - JWT_SSO_TOKEN_URI={{JWT_SSO_TOKEN_URI}}
      - WECUBE_PLUGIN_DEPLOY_PATH={{WECUBE_PLUGIN_DEPLOY_PATH}}
      - WECUBE_SERVER_JMX_PORT={{WECUBE_SERVER_JMX_PORT}}
      - WECUBE_BUCKET={{WECUBE_BUCKET}}
      - WECUBE_CORE_HOST={{WECUBE_CORE_HOST}}
      - WECUBE_CUSTOM_PARAM={{WECUBE_CUSTOM_PARAM}}
      - APP_LOG_PATH={{APP_LOG_PATH}}
      
  auth-server:
    image: {{AUTH_SERVER_IMAGE_NAME}}:{{AUTH_SERVER_IMAGE_VERSION}}
    restart: always
    volumes:
      - /data/auth_server/log:{{AUTH_SERVER_LOG_PATH}}
      - /etc/localtime:/etc/localtime
      - {{DOCKER_API_CERTS_PATH}}:/certs
    ports:
      - {{AUTH_SERVER_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - MYSQL_SERVER_ADDR={{AUTH_SERVER_MYSQL_ADDR}}
      - MYSQL_SERVER_PORT={{AUTH_SERVER_MYSQL_PORT}}
      - MYSQL_SERVER_DATABASE_NAME={{AUTH_SERVER_DATABASE_NAME}}
      - MYSQL_USER_NAME={{AUTH_SERVER_MYSQL_USER_NAME}}
      - MYSQL_USER_PASSWORD={{AUTH_SERVER_MYSQL_USER_PASSWORD}}
      - AUTH_CUSTOM_PARAM={{AUTH_CUSTOM_PARAM}}
      - AUTH_SERVER_LOG_PATH={{AUTH_SERVER_LOG_PATH}}
      - USER_ACCESS_TOKEN={{USER_ACCESS_TOKEN}}
      - USER_REFRESH_TOKEN={{USER_REFRESH_TOKEN}}
      - JWT_PRI_KEY_PATH={{JWT_PRI_KEY_PATH}}
      - JWT_PUB_KEY_PATH={{JWT_PUB_KEY_PATH}}
      - LOG_LEVEL={{LOG_LEVEL}}
      

   
