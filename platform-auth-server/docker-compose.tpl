version: '3'
services:
  platform-auth-server:
    image: platform-auth-server:{{version}}
    container_name: platform-auth-server-{{version}}
    restart: always
    volumes:
      - /etc/localtime:/etc/localtime
      - [#path]/platform-auth-server/logs:/app/platform-auth-server/logs
      - [#path]/platform-auth-server/certs:/app/platform-auth-server/config/certs
    ports:
      - "[@HOSTIP]:[#http_port]:8080"
    environment:
      - LOG_LEVEL=[#LOG_LEVEL]
      - PASSWORD_PRIVATE_KEY_PATH=/app/platform-auth-server/config/certs/[#WECUBE_PRIVATE_KEY]
      - MYSQL_SERVER_ADDR=[#MYSQL_SERVER_ADDR]
      - MYSQL_SERVER_PORT=[#MYSQL_SERVER_PORT]
      - MYSQL_USER_NAME=[#MYSQL_USER_NAME]
      - MYSQL_USER_PASSWORD=[#MYSQL_USER_PASSWORD]
      - MYSQL_SERVER_DATABASE_NAME=[#MYSQL_SERVER_DATABASE_NAME]
      - SIGNING_KEY=[#SIGNING_KEY]
      - USER_ACCESS_TOKEN=[#USER_ACCESS_TOKEN]
      - USER_REFRESH_TOKEN=[#USER_REFRESH_TOKEN]
      - WECUBE_CORE_ADDRESS=[#WECUBE_CORE_ADDRESS]
      - SENDER_NAME=[#SENDER_NAME]
      - SENDER_MAIL=[#SENDER_MAIL]
      - AUTH_SERVER=[#AUTH_SERVER]
      - AUTH_PASSWORD=[#AUTH_PASSWORD]
      - SSL=[#SSL]
      - NOTIFY_PERCENT=[#NOTIFY_PERCENT]
