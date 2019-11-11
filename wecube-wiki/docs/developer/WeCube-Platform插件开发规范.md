# WeCube-Platform 插件开发规范

如对WeCube不够了解，请先阅读：[WeCube系统框架](https://blog.csdn.net/weixin_41547131/article/details/100620479)

### 插件是什么？
插件，对WeCube来说就是管理某个资源的所有服务。一般插件的服务都是以API的形式提供的。     
例如，腾讯云虚拟机服务就是WeCube中的一个插件，这个插件可以提供创建、销毁、启动、停止等一系列服务。     
再例如，CMDB的一个CI是WeCube中的一个插件，这个CI可以提供添加数据、修改数据、查询数据、删除数据的服务。

### 为什么需要插件？
WeCube-Platform作为一个一站式IT架构管理和运维管理工具平台，本身不具备任何IT运维工具的能力，其自身的能力是需要通过插件进行拓展和延伸的。
在实际应用中，插件都是以多个插件组成一个插件集合包来提供服务的。所以，我们所说的插件开发，一般都是插件集合包开发。

### WeCube能提供什么给插件？
WeCube作为插件运行平台，可以提供以下能力：  
1.docker容器 - 运行插件应用程序  
2.静态资源服务器 - 部署插件的静态资源  
3.mysql数据库  
4.MinIO对象存储桶  

### WeCube需要什么插件？
由于WeCube依赖插件提供能力，所以插件必须是可提供一部分IT架构管理能力或者一部分运维管理能力。
但是WeCube只有一个，插件有多个，所以WeCube需要定义一套规范，以便插件按照规范实现，才能注册到WeCube上运行。

### 插件是怎么运行在WeCube平台上的？  
步骤如下：  
step1：WeCube管理员拿到插件开发者发布插件包（xxx-plugin.zip）,使用WeCube的“插件包上传”功能，选择对应插件包进行上传；  
step2: 预览“插件配置”页面后，点击插件包注册；若插件有网页静态资源，此时会部署到静态资源服务器。  
step3: 进行“服务注册”；  
step4: 使用“运行管理”功能，选择一台容器母机来运行插件容器实例。  
step5: WeCube通过服务注册的信息去调用插件容器实例上的服务，或者WeCube通过网页静态资源访问插件容器实例上的服务。

### 插件的开发规范 
WeCube目前支持以下几种插件：
1.仅后端API服务， 示例：Qcloud资源管理、Saltstack部署管理
2.后端API服务+前端UI页面， 示例：Monitor
3.后端API服务+前端UI页面+MySql数据库， 示例： WeCMDB、Service-Mgmt

所以我们分别对注册描述文件、后端API服务、前端UI页面、MySql数据库提出了规范，另外每个插件必须有注册描述文件，和提供插件包。

##### 注册描述文件规范
必须命名为register.xml, [具体示例在此](https://github.com/WeBankPartners/wecube-platform/blob/dev/wecube-wiki/docs/developer/wecube_developer_package_XML_guide.md)


##### 后端API服务规范
1. 提供镜像包
1.1 镜像包必须命名为image.tar，仅支持tar格式，并要求docker load出来的image要求：repository必须是插件包名，tag是版本号;
1.2 提供的所有API的URL必须以/plugin-name/version/开头，plugin-name是插件包名，使用纯小写字母，多于一个单词时使用“-”连接，version是插件包的版本号；例如,/cmdb/v1/xxx ,/service-mgmt/v2/xxx ；
1.3 必须为声明的模型提供两个数据查询API  
    根据主键（接口参数命名为“id”）查询数据；
    根据其他任意一个属性值（除主键外）查询数据；
1.4 必须提供日志查询API（url及参数待定）
1.5 在register.xml里resourceDependencies部分，必须声明docker标签，并描述以下属性：
```
imageName  -- 镜像名，对应镜像包load出来的镜像，例如：service-mt:v0.6 、wecmdb:v0.1
containerName  --  容器名，为了避免容器重名， 建议命名为镜像包+版本号，例如： service-mt-v0.6、 wecmdb-v0.1
portBindings  -- 端口映射参数，必须是“{{host_port}}:21000”格式，{{host_port}}是WeCube分配的端口，冒号后面是容器提供服务的端口
volumeBindings  -- 卷绑定参数，必须是“{{base_mount_path}}/service-mt/log:/log”格式， {{base_mount_path}}是WeCube配置的默认绑定路径
envVariables  -- 容器环境变量， 容器启动所需参数，目前仅支持四个参数DATA_SOURCE_URL='{{data_source_url}}',DB_USER={{db_user}},DB_PWD={{db_password}},CORE_ADDR={{core_addr}}， 前三个是插件数据库连接信息，CORE_ADDR是WeCube的地址，主要为了插件能够调用core的接口
```
1.6  如有对外提供服务的插件接口，需要在register.xml里plugins部分声明；
plugin声明插件的name 和对应的entity（和dataModel种的entity对应）；
interface声明单个接口的action、path、http method；
inputParameters声明接口的输入参数；
outputParameters声明接口的输出参数；
parameter声明每个参数的属性；
```
datatype  --  数据类型
mappingType  --  参数类型，目前支持system_variable、context、entity三种
required  --  是否必输
mappingEntityExpression  -- 模型表达式，当mappingType为entity的时候，该字段有效
```

##### 前端UI页面规范
1.必须在register.xml的menus标签内声明菜单对应的访问页面相对路径；
 menus标签内容中cat必须是Portal提供6大菜单（任务JOBS、设计DESIGNING、实现IMPLEMENTATION、监测MONITORING、调整ADJUSTMENT、智慧INTELLIGENCE）之一， 并且code要保持唯一，不允许与已注册的code重复。
 例如，在任务JOBS菜单下注入“任务管理TASK_MANAGEMENT”菜单，该菜单对应的前端页面是前端资源包中的/task-management：
```
<menus>
    <menu code='JOBS_TASK_MANAGEMENT' cat='JOBS' displayName="Task Management">/task-management</menu>
</menus>
```
2.必须提供静态资源包，命名和格式都固定为ui.zip，目录结构如下：
```
ui.zip
└─dist
    ├─css
    ├─fonts
    ├─img
    └─js
```
3.符合插件[前端开发规范](
https://github.com/WeBankPartners/wecube-platform/blob/dev/wecube-wiki/docs/%E5%89%8D%E7%AB%AF%E6%8F%92%E4%BB%B6%E5%8C%96%E6%94%B9%E9%80%A0%E6%96%B9%E6%A1%88.md);

##### MySql数据库规范
1. 需要在register.xml里resourceDependencies部分声明，schema是申请的数据库schema，initFileName是创建表的sql脚本，upgradeFileName是升级数据表结构的脚本。
2. 必须提供初始化数据表的init.sql；
3. 如果已存在旧的插件版本，必须提供upgrade.sql，当进行版本升级时需要执行（当前仅支持连续版本升级，如1.1.1升到1.1.2， 或1.3升到1.4，不支持跨版本升级）;



##### 插件包规范
 - 命名必须是全小写英文字母，单词之间使用“-”来连接；
 - 必须是zip包；文件名必须为“插件包名-版本号.zip”；
 - 包目录结构如下（以WeCMDB插件、v1.0版本示例）：
```
wecmdb-v1.0.zip 
 +-  register.xml                         -- [必选] 注册描述文件
 +-  image.tar                            -- [必选] 后端服务docker镜像包
 +-  ui.zip                               -- [可选] 前端资源包
 +-  init.sql                             -- [可选] 初始化数据库表结构sql
 +-  upgrade.sql                          -- [可选] 连续版本升级数据库sql
```
