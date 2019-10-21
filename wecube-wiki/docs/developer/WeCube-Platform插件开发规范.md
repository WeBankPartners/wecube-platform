# WeCube-Platform 插件开发规范

如对WeCube不够了解，请先阅读：[WeCube系统框架](https://blog.csdn.net/weixin_41547131/article/details/100620479)

### 为什么需要插件
WeCube-Platform作为一个一站式IT架构管理和运维管理工具平台，本身不具备任何IT运维工具的能力，其自身的能力是需要通过插件进行拓展和延伸的。

### 插件的定义
插件，对WeCube来说就是管理某个资源的所有服务。一般插件的服务都是以API的形式提供的。     
例如，腾讯云虚拟机服务就是WeCube中的一个插件，这个插件可以提供创建、销毁、启动、停止等一系列服务。     
再例如，CMDB的一个CI是WeCube中的一个插件，这个CI可以提供添加数据、修改数据、查询数据、删除数据的服务。

在实际应用中，插件都是以多个插件组成一个插件集合包来提供服务的。所以，我们所说的插件开发，一般都是插件集合包开发。

### 插件的规范
所以我们需要基于WeCube平台可以提供docker容器、mysql数据库、MinIO对象存储库三种运行资源，定义一套插件开发规范，用来指导开发者进行插件开发。

##### 第一，WeCube对插件包要求有：
 - 命名必须是全小写英文字母，单词之间使用“-”来连接；
 - 必须是zip包；文件名必须为“插件包名-版本号.zip”；
 - 包目录结构如下（以ITSM插件、v1.0版本示例）：
```
itsm-v1.0.zip 
 +-  register.xml                    -- 注册描述文件, 必须包含
 +-  itsm-v1.0.tar                   -- 后端服务docker镜像包，命名必须是“插件包名-版本号”，单词之间使用“-”来连接， 必须包含
 +-  itsm-ui-v1.0.zip              -- 前端资源包， 可选
 +-  init.sql                             -- 初始化数据库表结构sql， 可选
 +-  upgrade.sql                    -- 连续版本升级数据库sql， 可选
```

##### 第二，若插件有前端，对前端资源包的要求有：
 - 文件名必须为“插件包名-ui-版本号.zip”，目录结构（itsm-ui-v1.0.zip）如下：
```
itsm-ui-v1.0.zip
└─dist
    ├─css
    ├─fonts
    ├─img
    └─js
```
 - 必须在register.xml中<menus>部分定义访问页面的相对路径。
 
##### 第三，若插件需要mysql数据库，要求有：
 - 必须提供初始化数据表的init.sql；
 - 必须在register.xml中<resourceDepedencies>部分定义<mysql>
 - 如果已存在旧的插件版本，必须提供upgrade.sql，当进行版本升级时需要执行（当前仅支持连续版本升级，如1.1.1升到1.1.2， 或1.3升到1.4，不支持跨版本升级）;
 
##### 第四，若插件需要S3存储库，要求有：
 - 必须register.xml中声明

##### 第二，register.xml要求必须包含7部分内容,[示例在此](https://github.com/WeBankPartners/wecube-platform/blob/420_plugin_dev_standard/wecube-wiki/docs/developer/wecube_developer_package_XML_guide.md)。
 - 1.依赖说明
 主要描述本插件运行时依赖的其他插件（插件名和版本）；
 - 2.插件菜单
 主要描述运行本插件包需要注入的菜单；
 注入的munu code必须在Portal提供6大菜单（任务JOBS、设计DESIGNING、实现IMPLEMENTATION、监测MONITORING、调整ADJUSTMENT、智慧INTELLIGENCE）下。
 例如，在任务JOBS菜单下注入“任务管理TASK_MANAGEMENT”菜单，该菜单对应的前端页面是前端资源包中的/task-management：
 ```
<menus>
    <menu code='JOBS_TASK_MANAGEMENT' cat='JOBS' displayName="Task Management">/task-management</menu>
</menus>
 ```
 
 - 3.数据模型
 描述本插件包对外提供服务的数据模型；
 一般提供的服务有增删改查四个API，其中查询API必须支持每个属性过滤查询的API;
 例如，服务管理插件提供了‘任务’这个模型，那么它提供的接口有：
```
POST /service-management/data-models/tasks
DELETE /service-management/data-models/tasks/{task-id}  - 根据唯一ID（主键）删除数据
PUT /service-management/data-models/tasks/{task-id}  - 根据唯一ID（主键）更新数据
GET /service-management/data-models/tasks?taskName=createTask  - 根据taskName属性过滤查询
```
 
 - 4.系统参数
 描述运行本插件包需要的系统参数；
 
 - 5.菜单权限
 描述第2部分注入的菜单权限；
 - 6.运行资源
 描述部署运行本插件包需要的基础资源(如容器、数据库、对象存储库等);
 - 7.插件配置
 描述本插件包对外提供的API服务。

##### 第三，插件包提供API的url要求是必须以插件包名为前缀；
插件包提供API（不论对外还是对本插件的前端）的url都要求必须以插件包名为前缀；
例如，服务管理插件包提供的API有以下这些：
```
POST /service-management/service-catalogues
PUT /service-management/tasks/{task-id}/process
GET /service-management/service-requests/{service-request-id}/attach-file
```
它们都是以/service-management开头的。
