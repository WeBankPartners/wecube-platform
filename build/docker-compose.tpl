version: '2'
services:
  wecube-portal:
    image: {{PORTAL_IMAGE}}:{{PORTAL_IMAGE_VERSION}}
    restart: always
    depends_on:
      - platform-gateway
      - platform-core
    volumes:
      - /data/wecube-portal/log:/var/log/nginx/
      - /etc/localtime:/etc/localtime
    ports:
      - {{PORTAL_PORT}}:8080
    environment:
      - GATEWAY_HOST={{GATEWAY_HOST}}
      - GATEWAY_PORT={{GATEWAY_PORT}}
      - TZ=Asia/Shanghai
    command: /bin/bash -c "envsubst < /etc/nginx/conf.d/mysite.template > /etc/nginx/conf.d/default.conf && exec nginx -g 'daemon off;'"

  platform-gateway:
    image: {{GATEWAY_IMAGE_NAME}}:{{GATEWAY_IMAGE_VERSION}}
    restart: always
    depends_on:
      - platform-core
    volumes:
      - /data/wecube/log:/log/ 
      - /etc/localtime:/etc/localtime
    ports:
      - {{GATEWAY_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - GATEWAY_ROUTE_CONFIG_SERVER={{GATEWAY_ROUTE_CONFIG_SERVER}}
      - GATEWAY_ROUTE_CONFIG_URI={{GATEWAY_ROUTE_CONFIG_URI}}
      - GATEWAY_ROUTE_ACCESS_KEY={{GATEWAY_ROUTE_ACCESS_KEY}}
      - GATEWAY_ROUTES_PLATFORM_CORE_URI={{GATEWAY_ROUTES_PLATFORM_CORE_URI}}

  platform-core:
    image: {{WECUBE_IMAGE_NAME}}:{{WECUBE_IMAGE_VERSION}}
    restart: always
    volumes:
      - /data/wecube/log:/log/ 
      - /etc/localtime:/etc/localtime
    ports:
      - {{WECUBE_SERVER_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - MYSQL_SERVER_ADDR={{MYSQL_SERVER_ADDR}}
      - MYSQL_SERVER_PORT={{MYSQL_SERVER_PORT}}
      - MYSQL_SERVER_DATABASE_NAME={{MYSQL_SERVER_DATABASE_NAME}}
      - MYSQL_USER_NAME={{MYSQL_USER_NAME}}
      - MYSQL_USER_PASSWORD={{MYSQL_ROOT_PASSWORD}}
      - CMDB_SERVER_URL={{CMDB_SERVER_URL}}
      - WECUBE_PLUGIN_HOSTS={{WECUBE_PLUGIN_HOSTS}}
      - WECUBE_PLUGIN_HOST_PORT={{WECUBE_PLUGIN_HOST_PORT}}
      - WECUBE_PLUGIN_HOST_USER={{WECUBE_PLUGIN_HOST_USER}}
      - WECUBE_PLUGIN_HOST_PWD={{WECUBE_PLUGIN_HOST_PWD}}
      - S3_ENDPOINT={{S3_URL}}
      - S3_ACCESS_KEY={{S3_ACCESS_KEY}}
      - S3_SECRET_KEY={{S3_SECRET_KEY}}

   
