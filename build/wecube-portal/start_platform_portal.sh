#!/bin/sh
cat /etc/nginx/conf.d/nginx.tpl > /etc/nginx/nginx.conf
sed -i "s~\${GATEWAY_HOST}~$GATEWAY_HOST~g" /etc/nginx/nginx.conf
sed -i "s~\${GATEWAY_PORT}~$GATEWAY_PORT~g" /etc/nginx/nginx.conf
sed -i "s~\${PUBLIC_DOMAIN}~$PUBLIC_DOMAIN~g" /etc/nginx/nginx.conf
exec nginx -g 'daemon off;'