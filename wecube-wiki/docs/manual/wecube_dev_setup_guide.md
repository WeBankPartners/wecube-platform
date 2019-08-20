# WeCube Developer Guide

## 介绍
WeCube主要使用Java进行研发，前端为Vue JS，数据库是MySQL，并依赖Tomcat运行。<br>
搭建WeCMDB研发环境分为3个步骤 - Java开发环境，搭建数据库，运行Tomcat <br>
## Java开发环境
TODO<br>
## 搭建数据库
步骤如下：
### Step 1: 安装数据库 (推荐使用MariaDB 10.1)
#### 请参考Mysql官方文档([Installing and Upgrading MySQL](https://dev.mysql.com/doc/refman/8.0/en/installing.html))<br>
### Step 2: 新建用户及数据库
#### 1.使用数据库root用户登陆数据库
`[root@VM_0_14_centos ~]# mysql -u root -p`
#### 2.新建数据库
`MariaDB [(none)]> create database wecube_db DEFAULT CHARSET utf8 COLLATE utf8_general_ci;` -- 库名可自行替换
#### 3.新建数据库用户,设置密码，并允许本地IP访问
`MariaDB [(none)]> create user 'wecub_user'@'localhost' identified by 'password';` --用户名和密码可自行替换
#### 4.允许其他IP访问（如无需要请跳过）
`MariaDB [(none)]> create user 'wecub_user'@'172.16.%.%' identified by 'password';` --允许172.16开头的IP访问
`MariaDB [(none)]> create user 'wecub_user'@'%' identified by 'password';` --允许外网所有IP访问
#### 5.将数据库所有table的权限grant给用户
`MariaDB [(none)]> grant all privileges on `wecube_db`.* to 'wecub_user'@'%';` 
#### 6.刷新授权
`MariaDB [(none)]> flush privileges;`
### Step 3: 初始化数据库
#### 1.使用Step 2新建的并且获得授权的数据库用户登陆MariaDB
`[root@VM_0_14_centos ~]# mysql -u wecube_user -p`
#### 2.使用新建的数据库
`MariaDB [(none)]> use wecube_db;`
#### 3.复制[WeCube数据库初始化表SQL](https://github.com/WeBankPartners/wecube-platform/blob/master/wecube-core/src/main/resources/database/01.wecube.schema.sql)到数据库中执行。
#### 4.复制[WeCube数据库初始化数据SQL](https://github.com/WeBankPartners/wecube-platform/blob/master/wecube-core/src/main/resources/database/02.wecube.system.data.sql)到数据库中执行。

确认所有步骤执行成功，至此，数据库搭建完毕。

## 运行Tomcat
TODO
