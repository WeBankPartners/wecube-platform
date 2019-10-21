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
 - 必须是zip包；文件名必须为“插件包名-版本号.zip”；
 - 包目录结构如下（以ITSM插件、v1.0版本示例）：
```
itsm-v1.0.zip 
 +-  register.xml                    -- 注册描述文件, 必须包含
 +-  itsm-v1.0.tar                   -- 后端服务docker镜像包， 必须包含
 +-  itsm-ui-v1.0.zip              -- 前端资源包， 可选
 +-  init.sql                             -- 初始化数据库表结构sql， 可选
 +-  upgrade.sql                    -- 连续版本升级数据库sql， 可选
```

  - 插件若有前端部分，必须在register.xml中<menus>部分定义访问页面的相对路径，而且前端资源包的文件名必须为“插件包名-ui-版本号.zip”，目录结构（itsm-ui-v1.0.zip）如下：
```
itsm-ui-v1.0.zip
└─dist
    ├─css
    ├─fonts
    ├─img
    └─js
```
- 若插件需要mysql数据库，必须提供初始化数据表的init.sql；必须在register.xml中<resourceDepedencies>部分定义<mysql>
  -  如果已存在旧的插件版本，必须提供upgrade.sql，当进行版本升级时需要执行（当前仅支持连续版本升级，如1.1.1升到1.1.2， 或1.3升到1.4，不支持跨版本升级）;

##### 第二，register.xml要求必须包含7部分内容,[示例在此](https://github.com/WeBankPartners/wecube-platform/blob/420_plugin_dev_standard/wecube-wiki/docs/developer/wecube_developer_package_XML_guide.md)。
 - 1.依赖说明，描述本插件包运行时依赖的其他插件；
 - 2.插件菜单，描述运行本插件包需要注入的菜单；
 - 3.数据模型，描述本插件包的数据模型；
 - 4.系统参数，描述运行本插件包需要的系统参数；
 - 5.菜单权限，描述第2部分注入的菜单权限；
 - 6.运行资源，描述部署运行本插件包需要的基础资源(如容器、数据库、对象存储库等)
 - 7.插件配置， -- Todo；

##### 第三，要求每个插件包必须对外提供两个数据模型接口和日志查询接口。
 - 1.根据唯一ID（string类型，兼容不同类型的唯一ID）查询数据模型记录 
 例如 服务目录插件的 服务目录插件：
 ```
 GET /data-models/service-catalogues/{service-catalogue-id}
 ```
 - 2.根据唯一ID更新数据模型记录
 如 服务目录插件的 服务目录插件：
 ```
 PUT /data-models/service-catalogues/{service-catalogue-id}
 ```
