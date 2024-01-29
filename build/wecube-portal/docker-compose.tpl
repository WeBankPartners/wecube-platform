version: '3'
services:
  wecube-portal:
    image: wecube-portal:{{version}}
    container_name: wecube-portal-{{version}}
    restart: always
    volumes:
      - /etc/localtime:/etc/localtime
      - [#path]/wecube-portal/log:/var/log/nginx/
      - [#path]/wecube-portal/data/ui-resources:/root/app/ui-resources
    ports:
      - "[@HOSTIP]:[#http_port]:8080"
    environment:
      - GATEWAY_HOST=[#GATEWAY_HOST]
      - GATEWAY_PORT=[#GATEWAY_PORT]
      - PUBLIC_DOMAIN=[#PUBLIC_DOMAIN]
      - TZ=Asia/Shanghai
    command: /bin/bash -c "/etc/nginx/start_platform_portal.sh"
