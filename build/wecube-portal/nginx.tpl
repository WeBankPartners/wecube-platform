user  root;
worker_processes  1;
error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;
events {
  worker_connections  1024;
}
http {
  include       /etc/nginx/mime.types;
  default_type  application/octet-stream;
  log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';
  access_log  /var/log/nginx/access.log  main;
  sendfile        on;
  keepalive_timeout  65;
  root   /root/app;
  upstream core {
        server ${GATEWAY_HOST}:${GATEWAY_PORT};
  }

  server {
        listen  8080;
        server_name     localhost;
	client_max_body_size 9999999m;
        client_header_timeout 99999999999s;
	keepalive_timeout 999999999s;

	location / {
                root /root/app;
        }
	location /platform {
		proxy_pass http://core;
	}
	location /service-mgmt {
		proxy_pass http://core;
	}
	location /wecmdb {
		proxy_pass http://core;
	}
	location /wecube-monitor {
		proxy_pass http://core;
	}
        location /weartifacts {
		proxy_pass http://core;
	}
   }
}

