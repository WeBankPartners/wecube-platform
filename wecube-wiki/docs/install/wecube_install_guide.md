# WeCube安装指引
WeCube运行环境包括3个组件：wecube-app、wecube-db(mysql)、minio(提供对象存储),这三个组件的安装包以docker镜像的方式提供，本安装指引通过docker-compose的方式启动WeCube服务，不需要单独安装mysql和minio对象存储；如果用户需要使用已存在的mysql和minio对象存储，修改部分配置文件即可。

## 安装前准备
1. 准备好一台linux主机，建议资源配置为4核8G。
2. 操作系统版本可以为ubuntu16.04以上或centos7.3以上。
3. 建议网络可通外网(需从外网下载部分文件)。
4. 安装docker1.17.03.x以上版本及docker-compose命令。
     - docker安装请参考[docker安装文档](https://github.com/WeBankPartners/we-cmdb/blob/master/cmdb-wiki/docs/install/docker_install_guide.md)
     - docker-compose安装请参考[docker-compose安装文档](https://github.com/WeBankPartners/we-cmdb/blob/master/cmdb-wiki/docs/install/docker-compose_install_guide.md)
5. 确认cmdb的api访问ip白名单列表中已包含wecube部署主机的ip。

## 配置
1. 建立执行目录和相关文件
   
   在部署机器上建立安装目录，新建以下三个文件:

   [wecube.cfg](../../../build/wecube.cfg)

   [install.sh](../../../build/install.sh)

   [docker-compose.tpl](../../../build/docker-compose.tpl)


2. 编辑wecube.cfg配置文件，该文件包含如下配置项，用户根据各自的部署环境替换掉相关值。

```
#wecube-core
wecube_server_ip={$wecube_server_ip}
wecube_server_port=9090
wecube_image_name={$wecube_image_name}
wecube_plugin_hosts=100.107.119.14,100.107.119.79
wecube_plugin_host_port=22
wecube_plugin_host_user={$plugin_host_user_name}
wecube_plugin_host_pwd={$plugin_host_password}

#cmdb
cmdb_url=http://{$cmdb_server_ip}:{$cmdb_server_port}/cmdb

#database
database_image_name={$cmdb_database_image_name}
database_init_password={$cmdb_database_init_password}

#cas
cas_url=http://{$cas_ip}:{$cas_port}/cas

#s3
s3_url=http://{$minio_server_ip}:9000
s3_access_key=access_key
s3_secret_key=secret_key
```

配置项                      |说明
---------------------------|--------------------
wecube_server_ip           |wecube的服务ip，cas单点登录成功后的回跳地址；如果浏览器是通过局域网访问，该值填部署主机的局域网ip;如果是公网访问需填公网可访问的ip地址，如LB的ip
wecube_server_port         |wecube的服务端口
wecube_image_name          |wecube的docker镜像名称
wecube_plugin_hosts        |wecube部署插件的容器主机ip
wecube_plugin_host_port    |wecube部署插件主机的ssh端口
wecube_plugin_host_user    |wecube部署插件主机的ssh用户
wecube_plugin_host_pwd     |wecube部署插件主机的ssh密码
cmdb_url                   |wecube依赖的cmdb服务url
database_image_name        |wecube数据库镜像名称
database_init_password     |wecube数据库初始化密码
cas_url                    |单点登陆cas服务器url
s3_url                     |wecube依赖的对象存储服务器地址，docker-compose.tpl中已经包含minio的S3服务，此处填部署主机ip
s3_access_key              |minio对象存储访问access_key
s3_secret_key              |minio对象存储访问secret_key

3. install.sh文件
```
#!/bin/bash
set -ex
if ! docker --version &> /dev/null
then
    echo "must have docker installed"
    exit 1
fi

if ! docker-compose --version &> /dev/null
then
    echo  "must have docker-compose installed"
    exit 1
fi

source wecube.cfg

sed  "s~{{WECUBE_DATABASE_IMAGE_NAME}}~$database_image_name~" docker-compose.tpl >  docker-compose.yml  
sed -i "s~{{WECUBE_IMAGE_NAME}}~$wecube_image_name~" docker-compose.yml  
sed -i "s~{{WECUBE_SERVER_PORT}}~$wecube_server_port~" docker-compose.yml 
sed -i "s~{{MYSQL_ROOT_PASSWORD}}~$database_user_password~" docker-compose.yml 
sed -i "s~{{CAS_SERVER_URL}}~$cas_url~" docker-compose.yml 
sed -i "s~{{CMDB_SERVER_URL}}~$cmdb_url~" docker-compose.yml 
sed -i "s~{{WECUBE_SERVER_IP}}~$wecube_server_ip~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOSTS}}~$wecube_plugin_hosts~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PORT}}~$wecube_plugin_host_port~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_USER}}~$wecube_plugin_host_user~" docker-compose.yml
sed -i "s~{{WECUBE_PLUGIN_HOST_PWD}}~$wecube_plugin_host_pwd~" docker-compose.yml
sed -i "s~{{S3_URL}}~$s3_url~" docker-compose.yml
sed -i "s~{{S3_ACCESS_KEY}}~$s3_access_key~" docker-compose.yml
sed -i "s~{{S3_SECRET_KEY}}~$s3_secret_key~" docker-compose.yml

docker-compose  -f docker-compose.yml  up -d

```

3. docker-compose.tpl文件
此文件中配置了要安装的服务:wecube、mysql和minio。
如果已有minio和mysql，在文件中将这两段注释掉,在wecube的environment配置中,手动修改s3和数据库配置即可。
详细代码如下:
```
version: '2'
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
      - /etc/localtime:/etc/localtime
    environment:
      - MINIO_ACCESS_KEY={{S3_ACCESS_KEY}}
      - MINIO_SECRET_KEY={{S3_SECRET_KEY}}
  mysql:
    image: {{WECUBE_DATABASE_IMAGE_NAME}}
    restart: always
    command: [
            '--character-set-server=utf8mb4',
            '--collation-server=utf8mb4_unicode_ci',
            '--default-time-zone=+8:00'
    ]
    environment:
      - MYSQL_ROOT_PASSWORD={{MYSQL_ROOT_PASSWORD}}
    volumes:
      - /data/wecube/db:/var/lib/mysql
      - /etc/localtime:/etc/localtime
  wecube:
    image: {{WECUBE_IMAGE_NAME}}
    restart: always
    depends_on:
      - mysql
    volumes:
      - /data/wecube/log:/log/ 
      - /etc/localtime:/etc/localtime
    networks:
      - wecube-core
    ports:
      - {{WECUBE_SERVER_PORT}}:8080
    environment:
      - TZ=Asia/Shanghai
      - MYSQL_SERVER_ADDR=mysql
      - MYSQL_SERVER_PORT=3306
      - MYSQL_SERVER_DATABASE_NAME=wecube
      - MYSQL_USER_NAME=root
      - MYSQL_USER_PASSWORD={{MYSQL_ROOT_PASSWORD}}
      - CAS_SERVER_URL={{CAS_SERVER_URL}}
      - CMDB_SERVER_URL={{CMDB_SERVER_URL}}
      - CAS_REDIRECT_APP_ADDR={{WECUBE_SERVER_IP}}:{{WECUBE_SERVER_PORT}}
      - WECUBE_PLUGIN_HOSTS={{WECUBE_PLUGIN_HOSTS}}
      - WECUBE_PLUGIN_HOST_PORT={{WECUBE_PLUGIN_HOST_PORT}}
      - WECUBE_PLUGIN_HOST_USER={{WECUBE_PLUGIN_HOST_USER}}
      - WECUBE_PLUGIN_HOST_PWD={{WECUBE_PLUGIN_HOST_PWD}}
      - S3_ENDPOINT={{S3_URL}}
      - S3_ACCESS_KEY={{S3_ACCESS_KEY}}
      - S3_SECRET_KEY={{S3_SECRET_KEY}}
```
## 执行安装
1. 执行如下命令，通过docker-compose拉起WeCube服务。
```
/bin/bash ./install.sh
```

4. 安装完成后，访问WeCube的url http://wecube_server_ip:wecube_server_port 确认页面访问正常。


