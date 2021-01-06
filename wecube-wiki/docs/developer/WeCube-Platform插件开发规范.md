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
2.后端API服务+前端UI页面， 示例：WeCMDB插件  
3.后端API服务+前端UI页面+MySql数据库， 示例：Service-Mgmt插件、Monitor插件  

所以我们分别对注册描述文件、后端API服务、前端UI页面、MySql数据库提出了规范，另外每个插件必须有注册描述文件，和提供插件包。

##### 注册描述文件规范
必须命名为register.xml, [具体示例在此](https://github.com/WeBankPartners/wecube-platform/blob/master/wecube-wiki/docs/developer/wecube_developer_package_XML_guide.md)  

##### 后端API服务规范
###### 启动及打包方式规范 
1.提供镜像包，目前仅支持以容器方式启动插件服务程序，  
2.镜像包必须命名为image.tar，仅支持tar格式，并要求docker load出来的image要求：repository必须是插件包名，tag是版本号;  
###### API的url规范
提供的所有API的URL必须以/plugin-name/version/开头，plugin-name是插件包名，使用纯小写字母，多于一个单词时使用“-”连接，version是插件包的版本号；例如,/cmdb/v1/xxx ,/service-mgmt/v2/xxx ；  
~~###### 数据模型查询API规范
必须为声明的模型提供两个数据查询API    
    根据主键（接口参数命名为“id”）查询数据；  
    根据其他任意一个属性值（除主键外）查询数据；~~  
~~###### 日志查询功能规范
必须提供日志查询API（url及参数待定）~~  
###### 容器启动参数的规范
在register.xml里resourceDependencies部分，必须声明docker标签，并描述以下属性：  
```
imageName  -- 镜像名，对应镜像包load出来的镜像，例如：service-mt:v0.6 、wecmdb:v0.1
containerName  --  容器名，为了避免容器重名， 建议命名为镜像包+版本号，例如： service-mt-v0.6、 wecmdb-v0.1
portBindings  -- 端口映射参数，必须是“{{ALLOCATE_PORT}}:21000”格式，{{ALLOCATE_PORT}}是WeCube分配的端口，冒号后面是容器提供服务的端口
volumeBindings  -- 卷绑定参数，必须是“{{BASE_MOUNT_PATH}}/service-mt/log:/log”格式， {{BASE_MOUNT_PATH}}是WeCube配置的默认绑定路径,可通过系统参数BASE_MOUNT_PATH配置
envVariables  -- 容器环境变量， 容器启动所需参数，例如envVariables="DB_HOST={{DB_HOST}},DB_PORT={{DB_PORT}},DB_SCHEMA={{DB_SCHEMA}},DB_USER={{DB_USER}},DB_PWD={{DB_PWD}},CORE_ADDR={{CORE_ADDR}}"
```
现在WeCube已支持容器启动的参数变量替换有：  
{{ALLOCATE_HOST}}  -  用户在页面选择的母机  
{{ALLOCATE_PORT}}  -  WeCube分配的端口  
{{DB_HOST}}  -  在WeCube申请mysql数据库的主机IP  
{{DB_PORT}}  -  在WeCube申请mysql数据库访问端口  
{{DB_SCHEMA}}  -  在WeCube申请mysql数据库的schema  
{{DB_USER}}  -  在WeCube申请mysql数据库的用户  
{{DB_PWD}}   -  在WeCube申请mysql数据库用户的密码  
{{CORE_ADDR}}  -  core地址（示例：http://127.0.0.1:19090）  
{{CMDB_URL}}  -  cmdb地址（示例：http://127.0.0.1:8080/wecmdb）  
{{BASE_MOUNT_PATH}}  -  容器母机绑定路径  
除以上的变量外，每个插件还可以自定义参数（注意不要与已有的参数名重复），步骤如下：  
```
1.在register.xml中的系统参数部分定义 新的系统参数，例如：
    <systemParameters>
        <systemParameter name="CMDB_VAR1" defaultValue='aaa' scopeType='global'/>
        <systemParameter name="CMDB_VAR1" defaultValue='bbb' scopeType='plugin-package'/>
    </systemParameters>
2.register.xml中的docker部分使用自定义参数：
    <resourceDependencies>
        <docker imageName="wecmdb:v0.1" containerName="wecube-plugins-wecmdb" portBindings="{{ALLOCATE_PORT}}:8081" envVariables="VAR1={{CMDB_VAR1}},VAR2={{CMDB_VAR2}}"/>
    </resourceDependencies>
3.插件包上传后，xml所定义的参数会写入到system_variable表里；
4.用户可以通过WeCube的“系统参数”菜单可以修改参数值（可选）
5.创建插件实例时，会找到envVariables中两个大括号中的变量名，然后以system_variable的value替换（若value为空，则使用defaultValue）。然后将替换后的值以env变量的方式传入到容器中。
```

###### 插件服务及接口规范
如有对外提供服务的插件接口，需要在register.xml里plugins部分声明；  
plugins里面可以有多个plugin，一个plugin里面可以有多个interface，一个interface下面有inputParameters和outputParameter，inputParameters和outputParameters里面都可以有多个parameter。
 - plugin声明插件的name 和对应的entity（和dataModel种的entity对应）,若不声明entity，也可以在插件注册页面让用户手动选择；  
 - interface声明单个API的action、path、httpMethod、isAsyncProcessing、type；  
```
action - API的主要动作，有多个单词请使用驼峰命名方式，不允许带空格或其他连接符
path - API的path
httpMethod - http方法，支持标准的http方法取值，纯大写字母，如'GET'、'POST'
isAsyncProcessing - 是否异步接口，取值范围有'Y'(是)和'N'（否）,默认'N'
type - 取值范围有'EXECUTION'（执行类）和'APPROVAL'（审批类）,不声明时默认'EXECUTION'
```
 - inputParameters声明接口的输入参数；  
 - outputParameters声明接口的输出参数；  
 - parameter声明每个参数的属性；  
```
datatype  --  数据类型
mappingType  --  参数类型，目前支持system_variable、context、entity三种
required  --  是否必输
mappingEntityExpression  -- 模型表达式，当mappingType为entity的时候，该字段有效
```

插件的interface对应的API的输入参数和输出参数规范如下：
```json
输入参数
{
    "requestId": "request-001",  //仅异步调用需要用到
    "operator": "admin",  //操作人
    "inputs": [  
        {},
        {},
        {}
    ]
}
```  
```json
输出参数
{
    "resultCode": "0",  //调用插件结果，"0"代表调用成功，"1"代表调用失败
    "resultMessage": "success",  //调用结果信息，一般用于调用失败时返回失败信息
    "results": {
        "outputs": [
            {},
            {},
            {}
        ]
    }
}
```  

输入参数中input数组的一个元素是一个json对象，它包含一个无需xml声明的属性（callbackParameter，类型String，同一个api请求中，input数组中的callbackParameter必须唯一，此字段会在返回参数中的results.output中返回，用于定位input数组中的每个元素的返回结果），其他的每个属性都需要定义在inputParameters标签的parameter中；
输出参数中results.output数组的一个元素也是一个json对象，如上所述，它包含一个无需xml声明的属性（callbackParameter，类型String），其他的每个属性都需要定义在outputParameters标签的parameter中，并且固定包含以下两个属性
 - errorCode  //String类型，"0"代表成功，"1"代表失败
 - errorMessage  //String类型，当errorCode="1"时返回失败信息

示例如下：
下面是xml中的interface定义，
```
<interface action="create" path="/service-mgmt/v1/tasks" httpMethod='POST' isAsyncProcessing="Y" type="APPROVAL">
    <inputParameters>
        <parameter datatype="string" mappingType='system_variable' mappingSystemVariableName='CALLBACK_URL' required='Y'>
            callbackUrl
        </parameter>
        <parameter datatype="string" mappingType='constant' required='Y'>taskName</parameter>
        <parameter datatype="string" mappingType='constant' required='Y'>roleName</parameter>
    </inputParameters>
    <outputParameters>
        <parameter datatype="string">errorCode</parameter>
        <parameter datatype="string">errorMessage</parameter>
        <parameter datatype="string">taskResult</parameter>
    </outputParameters>
</interface>
```
那么，这个interface请求的json是，  
```json
{
    "requestId": "request-001",
    "operator": "admin",
    "inputs": [
        {
            "callbackParameter": "callback001",
            "taskName": "task-001",
            "roleName": "admin",
            "callbackUrl": "/v1/process/instances/callback"
        },
        {
            "callbackParameter": "callback002",
            "taskName": "task-002",
            "roleName": "admin",
            "callbackUrl": "/v1/process/instances/callback"
        }
    ]
}
```
这个interface返回的json是，  
```
{
    "resultCode": "0",
    "resultMessage": "success",
    "results": {
        "outputs": [
            {
                "callbackParameter": "callback001",
                "errorCode": "0",
                "errorMessage": "",
                "taskResult": "Approve"
            },
            {
                "callbackParameter": "callback002",
                "errorCode": "1",
                "errorMessage": "Reject this request",
                "taskResult": "Reject"
            }
        ]
    }
}
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
~~3. 如果已存在旧的插件版本，必须提供upgrade.sql，当进行版本升级时需要执行（当前仅支持连续版本升级，如1.1.1升到1.1.2， 或1.3升到1.4，不支持跨版本升级）;~~  

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
