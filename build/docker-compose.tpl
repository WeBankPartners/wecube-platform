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
  wecube-app:
    image: {{WECUBE_IMAGE_NAME}}:{{IMAGE_VERSION}}
    restart: always
    depends_on:
      - wecube-minio
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

   
