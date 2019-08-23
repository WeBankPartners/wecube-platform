version: '2'
networks:
  wecube-core:
    external: false
services:
  minio:
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
    environment:
      - MINIO_ACCESS_KEY={{S3_ACCESS_KEY}}
      - MINIO_SECRET_KEY={{S3_SECRET_KEY}}
    networks:
      - wecube-core
  mysql:
    image: {{WECUBE_DATABASE_IMAGE_NAME}}
    restart: always
    command: [
            '--character-set-server=utf8mb4',
            '--collation-server=utf8mb4_unicode_ci',
            '--default-time-zone=+8:00'
    ]
    environment:
      - MYSQL_ROOT_PASSWORD={{MYSQL_USER_PASSWORD}}
    networks:
      - wecube-core
    volumes:
      - /data/wecube/db:/var/lib/mysql
  wecube:
    image: {{WECUBE_CORE_IMAGE_NAME}}
    restart: always
    depends_on:
      - mysql
    volumes:
      - /data/wecube/log:/data/ 
    networks:
      - wecube-core
    ports:
      - {{WECUBE_CORE_EXTERNAL_PORT}}:8080
    environment:
      - MYSQL_SERVER_ADDR=mysql
      - MYSQL_SERVER_PORT=3306
      - MYSQL_SERVER_DATABASE_NAME=wecube
      - MYSQL_USER_NAME=root
      - MYSQL_USER_PASSWORD={{MYSQL_USER_PASSWORD}}
      - CAS_SERVER_URL={{CAS_SERVER_URL}}
      - CMDB_SERVER_URL={{CMDB_SERVER_URL}}
      - CAS_REDIRECT_APP_ADDR={{WECUBE_CORE_EXTERNAL_IP}}:{{WECUBE_CORE_EXTERNAL_PORT}}
      - WECUBE_PLUGIN_HOSTS={{WECUBE_PLUGIN_HOSTS}}
      - WECUBE_PLUGIN_HOST_PORT={{WECUBE_PLUGIN_HOST_PORT}}
      - WECUBE_PLUGIN_HOST_USER={{WECUBE_PLUGIN_HOST_USER}}
      - WECUBE_PLUGIN_HOST_PWD={{WECUBE_PLUGIN_HOST_PWD}}
      - S3_ENDPOINT={{S3_ENDPOINT}}
      - S3_ACCESS_KEY={{S3_ACCESS_KEY}}
      - S3_SECRET_KEY={{S3_SECRET_KEY}}

   
