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
  wecube-mysql:
    image: {{WECUBE_DATABASE_IMAGE_NAME}}
    restart: always
    command: [
            '--character-set-server=utf8mb4',
            '--collation-server=utf8mb4_unicode_ci',
            '--default-time-zone=+8:00'
    ]
    ports:
      - 13306:3306
    environment:
      - MYSQL_ROOT_PASSWORD={{MYSQL_ROOT_PASSWORD}}
    volumes:
      - /data/wecube/db:/var/lib/mysql
      - /etc/localtime:/etc/localtime
  wecube-app:
    image: {{WECUBE_IMAGE_NAME}}
    restart: always
    depends_on:
      - wecube-minio
      - wecube-mysql
    volumes:
      - /var/log/wecube:/var/log/wecube
      - /etc/localtime:/etc/localtime
    ports:
      - {{WECUBE_SERVER_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - MYSQL_SERVER_ADDR=wecube-mysql
      - MYSQL_SERVER_PORT=3306
      - MYSQL_SERVER_DATABASE_NAME=wecube
      - MYSQL_USER_NAME=root
      - MYSQL_USER_PASSWORD={{MYSQL_ROOT_PASSWORD}}
      - CAS_SERVER_URL={{CAS_SERVER_URL}}
      - CAS_REDIRECT_APP_ADDR={{WECUBE_SERVER_IP}}:{{WECUBE_SERVER_PORT}}
      - WECUBE_PLUGIN_HOSTS={{WECUBE_PLUGIN_HOSTS}}
      - WECUBE_PLUGIN_HOST_PORT={{WECUBE_PLUGIN_HOST_PORT}}
      - WECUBE_PLUGIN_HOST_USER={{WECUBE_PLUGIN_HOST_USER}}
      - WECUBE_PLUGIN_HOST_PWD={{WECUBE_PLUGIN_HOST_PWD}}
      - S3_ENDPOINT={{S3_URL}}
      - S3_ACCESS_KEY={{S3_ACCESS_KEY}}
      - S3_SECRET_KEY={{S3_SECRET_KEY}}

   
