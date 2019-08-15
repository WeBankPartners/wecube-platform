# WeCube Install Guide

## 安装前准备
1. 准备好一台主机最少资源配置为4核8GB。
2. 操作系统版本可以为ubuntu16.04以上或centos7.3以上。
3. 建议网络可通外网(需从外网下载docker运行镜像)。
4. 安装docker1.17.03.x以上。
5. 安装docker-compose命令。

## 安装过程
1. 通过github拉取WeCube代码。
2. 进入代码目录，编辑wecube-core.cfg文件，其中的配置项如下，根据主机环境替换掉相关的值：

配置项                      |说明
---------------------------|--------------------
wecube_core_external_port  |wecube-core的外部访问端口
wecube_core_exteranl_ip    |wecube-core的外部访问ip
wecube_image_name          |wecube-core的docker镜像名称
wecube_plugin_hosts        |wecube-core部署插件的容器主机IP
wecube_plugin_host_port    |wecube-core部署插件主机的ssh端口
wecube_plugin_host_user    |wecube-core部署插件主机的ssh用户
wecube_plugin_host_pwd     |wecube-core部署插件主机的ssh密码
cmdb_url                   |wecube-core操作的cmdb url
database_image_name        |wecube-core依赖的数据库镜像
database_user_password     |wecube-core依赖的数据库初始化密码
cas_url                    |wecube-core对应的cas地址
s3_endpoint                |wecube-core依赖的s3对象存储服务器地址，docker-compose.tpl中已经包含minio的S3服务，此处用部署主机的ip
s3_access_key              |minio S3对象存储访问的access_key
s3_secret_key              |minio S3对象存储访问的secret_key


3. 执行如下命令，通过docker-compose拉起WeCube服务
```
/bin/bash ./install.sh
```

4. 安装完成后，访问WeCube的url http://ip:port 确认页面正常显示。


