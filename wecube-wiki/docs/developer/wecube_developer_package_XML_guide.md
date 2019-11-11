# 插件开发 xml 文件编写规范

## 示例 xml 文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<package name="service-management" version="v0.2">

    <!-- 1.依赖分析 - 描述运行本插件包需要的其他插件包 -->
    <packageDependencies>
        <packageDependency name='xxx' version='1.0'/>
        <packageDependency name='xxx233' version='1.5'/>
    </packageDependencies>

    <!-- 2.菜单注入 - 描述运行本插件包需要注入的菜单 -->
    <menus>
        <menu code='JOBS_SERVICE_CATALOG_MANAGEMENT' cat='JOBS' displayName="Servive Catalog Management">/service-catalog</menu>
        <menu code='JOBS_TASK_MANAGEMENT' cat='JOBS' displayName="Task Management">/task-management</menu>
    </menus>

    <!-- 3.数据模型 - 描述本插件包的数据模型,并且描述和Framework数据模型的关系 -->
    <dataModel>
        <entity name="service_catalogue" displayName="服务目录" description="服务目录模型">
            <attribute name="id" datatype="int" description="唯一ID"/>
            <attribute name="name" datatype="string" description="名字"/>
            <attribute name="status" datatype="string" description="状态"/>
        </entity>
        <entity name="service_pipeline" displayName="服务通道" description="服务通道模型">
            <attribute name="id" datatype="int" description="唯一ID"/>
            <attribute name="service_catalogue_id" datatype='ref' description="所属服务目录"
                       refPackage="" refVersion="" refEntity="service_catalogue" ref="id"/>
            <attribute name="name" datatype="string" description="名字"/>
            <attribute name="owner_role_id" datatype="int" description="所属角色"/> <!--应该是ref到角色ID，目前暂无-->
            <attribute name="status" datatype="string" description="状态"/>
        </entity>
        <entity name="service_request_template" displayName="服务请求模板" description="服务请求模板模型">
            <attribute name="id" datatype="int" description="唯一ID"/>
            <attribute name="service_pipeline_id" datatype='ref' description="所属服务通道"
                       refPackage="" refVersion="" refEntity="service_pipeline" ref="id"/>
            <attribute name="name" datatype="string" description="名字"/>
            <attribute name="process_defined_key" datatype="string" description="名字"/>
            <attribute name="status" datatype="string" description="状态"/>
        </entity>
        <entity name="service_request" displayName="服务请求" description="服务请求模型">
            <attribute name="id" datatype="int" description="唯一ID"/>
            <attribute name="template_id" datatype='ref' description="所属服务模板"
                       refPackage="" refVersion="" refEntity="service_request_template" ref="id"/>
            <attribute name="reporter" datatype="string" description="上报人"/>
            <attribute name="reporter_role_id" datatype="string" description="上报角色ID"/>
            <attribute name="report_time" datatype="timestamp" description="上报时间"/>
            <attribute name="emergency" datatype="string" description="紧急程度"/>
            <attribute name="result" datatype="string" description="结果"/>
            <attribute name="process_instance_id" datatype="string" description="流程模板ID"/>
            <attribute name="status" datatype="string" description="状态"/>
        </entity>
        <entity name="task" displayName="任务" description="任务模型">
            <attribute name="id" datatype="int" description="唯一ID"/>
            <attribute name="service_request_id" datatype='ref' description="所属服务请求"
                       refPackage="" refVersion="" refEntity="service_request" ref="id"/>
            <attribute name="process_instance_id" datatype="string" description="流程实例ID"/>
            <attribute name="callback_url" datatype="int" description="回调url"/>
            <attribute name="name" datatype="int" description="任务名称"/>
            <attribute name="process_definition_key" datatype="int" description="流程模板ID"/>
            <attribute name="reporter" datatype="string" description="上报人"/>
            <attribute name="report_time" datatype="timestamp" description="上报时间"/>
            <attribute name="operator_role_id" datatype="string" description="操作角色ID"/>
            <attribute name="operator" datatype="string" description="操作人"/>
            <attribute name="operator_time" datatype="timestamp" description="操作时间"/>
            <attribute name="input_parameters" datatype="int" description="输入参数"/>
            <attribute name="result" datatype="int" description="结果"/>
            <attribute name="result_message" datatype="int" description="处理结果"/>
            <attribute name="status" datatype="string" description="状态"/>
        </entity>
    </dataModel>

    <!-- 4.系统参数 - 描述运行本插件包需要的系统参数 -->
    <systemParameters>
        <systemParameter name="xxx" defaultValue='xxxx' scopeType='global'/>
        <systemParameter name="xxx" defaultValue='xxxx' scopeType='plugin-package'/>
    </systemParameters>

    <!-- 5.权限设定 -->
    <authorities>
        <authority systemRoleName="admin" >
            <menu code="JOBS_SERVICE_CATALOG_MANAGEMENT" />
            <menu code="JOBS_TASK_MANAGEMENT" />
        </authority >
        <authority systemRoleName="wecube_operator" >
            <menu code="JOBS_TASK_MANAGEMENT" />
        </authority >
    </authorities>

    <!-- 6.运行资源 - 描述部署运行本插件包需要的基础资源(如主机、虚拟机、容器、数据库等) -->
    <resourceDependencies>
        <docker imageName="service_management" containerName="service_management" portBindings="22000:21000" volumeBindings="" envVariables="-e DATA_SOURCE_URL={%s} -e DB_USER={%s} -e DB_PWD={%s} -e CORE_ADDR={%s}"/>
        <mysql schema="service_management" initFileName="init.sql" upgradeFileName="upgrade.sql"/>
        <s3 bucketName="service_management"/>
    </resourceDependencies>

    <!-- 7.插件列表 - 描述插件包中单个插件的输入和输出 -->
    <plugins>
        <plugin name="task">
            <interface action="create" path="/service-management/tasks" httpMethod='POST'>
                <inputParameters>
                    <parameter datatype="string" mappingType='system_variable' mappingSystemVariableId='1' required='Y'>callbackUrl</parameter>
                    <parameter datatype="string" mappingType='context'  required='N'>description</parameter>
                    <parameter datatype="string" mappingType='context' required='Y'>name</parameter>
                    <parameter datatype="string" mappingType='entity' mappingEntityExpression='name_xxx' required='Y'>operatorRoleId</parameter>
                    <parameter datatype="string" mappingType='context' required='Y'>processDefinitionKey</parameter>
                    <parameter datatype="string" mappingType='context' required='Y'>processInstanceId</parameter>
                    <parameter datatype="string" mappingType='context' required='Y'>reporter</parameter>
                    <parameter datatype="string" mappingType='context' required='Y'>serviceRequestId</parameter>
                </inputParameters>
                <outputParameters>
                    <parameter datatype="string">status</parameter>
                    <parameter datatype="string">message</parameter>
                </outputParameters>
            </interface>
        </plugin>
        <plugin name="service request management">
            <interface action="update" path="/service-management/service-requests/{service-request-id}/done" httpMethod='PUT'>
                <inputParameters>
                    <parameter datatype="string" mappingType='system_variable' mappingSystemVariableId='1' required='Y'>service-request-id</parameter>
                    <parameter datatype="string" mappingType='context'  required='Y'>result</parameter>
                </inputParameters>
                <outputParameters>
                    <parameter datatype="string">status</parameter>
                    <parameter datatype="string">message</parameter>
                </outputParameters>
            </interface>
        </plugin>
    </plugins>
</package>
```

## 编写规范

一个插件 xml 文件包含以下几个tag

| tag 名称             | 描述     | 是否必须                   |
| -------------------- | -------- | -------------------------- |
| packageDependencies  | 依赖分析 | 否                         |
| menus                | 菜单注入 | 否                         |
| dataModel            | 数据模型 | 否                         |
| systemParameters     | 系统参数 | 否                         |
| authorities          | 权限设定 | 否                         |
| resourceDependencies | 运行资源 | 是且必须要有`<docker>`资源 |
| plugins              | 插件列表 | 否                         |

### packageDependencies

#### packageDependency

| tag 名称 | 描述 | 是否必须 |
| -------- | ---- | -------- |
| name     | 名称 | 是       |
| version  | 版本 | 是       |

### menus

#### menu

| tag 名称    | 描述         | 是否必须 |
| ----------- | ------------ | -------- |
| code        | 菜单识别码   | 是       |
| cat         | 菜单类别     | 否       |
| displayName | 前端展示名称 | 否       |

### dataModel

#### entity

| tag 名称    | 描述         | 是否必须 |
| ----------- | ------------ | -------- |
| name        | 名称         | 是       |
| displayName | 前端展示名称 | 是       |
| description | 描述         | 否       |

#### attribute 

| tag 名称    | 描述                  | 是否必须                        |
| ----------- | --------------------- | ------------------------------- |
| name        | 名称                  | 是                              |
| dataType    | 数据类型              | 是 (`int`, `str`, `ref`)        |
| description | 描述                  | 否                              |
| refPackage  | 引用的插件包名称      | 否 (若 `dataType`为`ref`则必填) |
| refVersion  | 引用的插件包版本      | 否 (若 `dataType`为`ref`则必填) |
| refEntity   | 引用的`entity`名称    | 否 (若 `dataType`为`ref`则必填) |
| ref         | 引用的`attribute`名称 | 否 (若 `dataType`为`ref`则必填) |

### systemParameters

#### systemParameter 

| tag 名称     | 描述       | 是否必须 |
| ------------ | ---------- | -------- |
| name         | 名称       | 是       |
| defaultValue | 默认值     | 否       |
| scopeType    | 作用域类型 | 是       |

### authorities

#### authority 

`authority `tag 包含 `menu` tag，具体属性参考`menu`



### resourceDependencies

#### docker

| tag 名称       | 描述       | 是否必须 |
| -------------- | ---------- | -------- |
| imageName      | 镜像名称   | 是       |
| containerName  | 容器名称   | 是       |
| portBindings   | 绑定端口号 | 是       |
| volumnBindings | 绑定数据卷 | 否       |
| envVariables   | 环境参数   | 否       |

#### mysql

| tag 名称        | 描述           | 是否必须 |
| --------------- | -------------- | -------- |
| schema          | 数据库名称     | 是       |
| initFileName    | 初始化脚本名称 | 是       |
| upgradeFileName | 更新脚本名称   | 否       |

#### s3

| tag 名称   | 描述       | 是否必须 |
| ---------- | ---------- | -------- |
| bucketName | 存储桶名称 | 是       |

### plugins

#### plugin

| tag 名称 | 描述     | 是否必须 |
| -------- | -------- | -------- |
| name     | 名称     | 是       |

##### interface

一个 plugin 里可有多个 interfaces



###### inputParameters 与 outputParameters

两个 tag 都使用了 `parameter` 的 tag

parameter：

`<parameter> </parameter>` 之间应当填写参数名称

| tag 名称              | 描述               | 是否必须 |
| --------------------- | ------------------ | -------- |
| dataType              | 数据类型           | 否       |
| mappingType           | 映射类型           | 否       |
| mappingSystemVariable | 映射系统参数id     | 否       |
| required              | 该paramter是否必须 | 是       |

