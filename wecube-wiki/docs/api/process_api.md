## WeCube 编排 API 概览：
#### GET: /platform/v1/process/definitions
查询编排列表
##### 输入参数：
参数名称|类型|必选|描述
:--|:--|:--|:--
includeDraft|string|否|是否包含草稿->0(不包含)| 1(包含)
permission|string|是|是使用还是管理->USE(使用) | MGMT(管理)

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]ProcessObj](#ProcessObj)|返回数据,编排列表

##### 示例：
请求 /platform/v1/process/definitions?includeDraft=0&permission=USE  

正常输出：
```
{
  "status": "OK",
  "message": "Success",
  "data": [
    {
      "procDefId": "tZqP4apq5g4W",
      "procDefKey": "wecube1584964208489",
      "procDefName": "041_ResourceInitializationBySubsystem_v1.0",
      "procDefVersion": "1",
      "status": "deployed",
      "procDefData": null,
      "rootEntity": "wecmdb:subsys",
      "createdTime": "2023-12-25 14:54:50",
      "excludeMode": "N",
      "tags": null
    }
  ]
}
```

#### GET: /platform/v1/process/definitions/{{procDefId}}/root-entities
编排根数据查询，查询结果是编排根ci的数据，结构由编排根ci所决定，比如wecmdb:subsys，那么其返回的结构是在cmdb中定义的subsys数据项的属性结构
##### 输入参数：
url参数  
参数名称|类型|必选|描述
:--|:--|:--|:--
procDefId|string|是|编排id


##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[]Map\[string\]object|返回数据，map数组

##### 示例：
请求： /platform/v1/process/definitions/tZwDxN9q5CxC/root-entities  
正常输出：
```
{
    "status": "OK",
    "message": "Success",
    "data": [
        {
            "security_zone": "security_zone_60b9d354f03dd37c",
            "code": "AUTH",
            "create_time": "2021-06-04 09:59:09",
            "displayName": "PRD_UM_AUTH_AUTH",
            "manage_role": "manage_role_60b9a90898f75150",
            "asset_id": "",
            "subsystem_design": "subsystem_design_60b9e0ea1b56f466",
            "confirm_time": "",
            "key_name": "PRD_UM_AUTH_AUTH",
            "update_time": "2022-12-30 14:27:05",
            "update_user": "wecube",
            "app_system": "app_system_60b9f9524832f644",
            "guid": "subsystem_60b9f96d2edae48b",
            "create_user": "umadmin",
            "id": "subsystem_60b9f96d2edae48b",
            "state": "created_0"
        }
    ]
}
```

#### GET: /platform/v1/process/definitions/{{procDefId}}/preview/entities/{{rootEntityId}}
编排数据预览接口，编排执行页面右边的预览图
##### 输入参数：
url参数  
参数名称|类型|必选|描述
:--|:--|:--|:--
procDefId|string|是|编排id
rootEntityId|string|是|根数据id

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]EntityPreview](#EntityPreview)|返回数据

##### 示例：
请求： /platform/v1/process/definitions/sOahgazW2BYd/preview/entities/subsystem_60b9f96d2edae48b    
正常输出：
```
{
    "status": "OK",
    "message": "Success",
    "data": {
        "entityTreeNodes": [
            {
                "packageName": "wecmdb",
                "entityName": "subsystem",
                "dataId": "subsystem_60b9f96d2edae48b",
                "displayName": "PRD_UM_AUTH_AUTH",
                "fullDataId": "subsystem_60b9f96d2edae48b",
                "entityData": null,
                "id": "wecmdb:subsystem:subsystem_60b9f96d2edae48b",
                "previousIds": [],
                "succeedingIds": [
                    "wecmdb:unit:unit_60b9f98933f260c9",
                    "wecmdb:unit:unit_60b9fd000623ac5b",
                    "wecmdb:unit:unit_60b9fe47899160b7"
                ]
            }
        ],
        "processSessionId": "856c087c-7620-4c8a-bd23-d04ced3b5bad"
    }
}
```

#### GET: /platform/v1/process/instances/tasknodes/session/{{sessionId}}/tasknode-bindings
编排绑定数据查询
##### 输入参数：
url参数，上一步预览请求返回的sessionId  
参数名称|类型|必选|描述
:--|:--|:--|:--
sessionId|string|是|sessionId

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]ProcessNode](#ProcessNode)|返回数据

##### 示例：
请求： /platform/v1/process/instances/tasknodes/session/856c087c-7620-4c8a-bd23-d04ced3b5bad/tasknode-bindings  
正常输出：
```
{
    "status": "OK",
    "message": "Success",
    "data": [
        {
            "nodeDefId": "sOahgaTW2C2B",
            "orderedNo": "3",
            "entityTypeId": "wecmdb:subsystem",
            "entityDataId": "subsystem_60b9f96d2edae48b",
            "bound": "Y"
        }
    ]
}
```

#### POST: /platform/v1/process/instances
发起编排
##### 输入参数：
参数名称|类型|必选|描述
:--|:--|:--|:--
entityDataId|string|是|根数据id
entityDisplayName|string|是|根数据显示名
entityTypeId|string|是|根数据ci
procDefId|string|是|编排id
processSessionId|string|是|预览的sessionId
taskNodeBinds|[\[\]ProcessNode](#ProcessNode)|是|绑定数据返回的数据


##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
id|int|编排实例id
entityDataId|string|根数据id
entityTypeId|string|根数据ci
procDefId|string|编排id
procDefKey|string|编排Key
procInstKey|string|编排实例Key
status|string|状态->InProgress(执⾏中) | Completed(完成) | 其他值都是失败
taskNodeInstances|[\[\]ProcessTaskNode](#ProcessTaskNode)|绑定数据返回的数据

##### 示例：
请求： /platform/v1/process/instances  
正常输入：
```
{
  "entityDataId": "subsystem_60b9f96d2edae48b",
  "processSessionId": "856c087c-7620-4c8a-bd23-d04ced3b5bad",
  "entityDisplayName": "PRD_UM_AUTH_AUTH",
  "entityTypeId": "wecmdb:subsystem",
  "procDefId": "sOahgazW2BYd",
  "taskNodeBinds": [
    {
      "entityDataId": "subsystem_60b9f96d2edae48b",
      "entityTypeId": "wecmdb:subsystem",
      "nodeDefId": "sOahgcmW2CkX",
      "orderedNo": "1"
    }
  ]
}
```
正常输出：
```
{
    "status": "OK",
    "message": "Success",
    "data": {
        "id": 622,
        "procInstKey": "tZCmbG4q5HU8",
        "operator": "umadmin",
        "status": "InProgress",
        "procDefId": "sOahgazW2BYd",
        "procDefKey": "wecube1634790851797",
        "entityTypeId": "wecmdb:subsystem",
        "entityDataId": "subsystem_60b9f96d2edae48b",
        "taskNodeInstances": [
            {
                "nodeId": "SubProcess_1oc2kus",
                "nodeName": "分配数据库资源",
                "nodeType": "subProcess",
                "nodeDefId": "sOahgaTW2C2B",
                "status": "NotStarted",
                "orderedNo": "3",
                "procDefId": "sOahgazW2BYd",
                "procDefKey": "wecube1634790851797",
                "routineExpression": null,
                "taskCategory": null,
                "serviceId": null,
                "dynamicBind": null,
                "description": null,
                "previousNodeIds": [
                    "SubProcess_0wm3io2"
                ],
                "succeedingNodeIds": [
                    "SubProcess_0bjiklu"
                ],
                "procInstId": 622,
                "procInstKey": "tZCmbG4q5HU8",
                "id": 3859
            }
        ]
    }
}
```

#### GET: /platform/v1/process/instances/{{processInstanceId}}
编排状态查询
##### 输入参数：
url参数，上一步发起编排请求返回的编排实例id  
参数名称|类型|必选|描述
:--|:--|:--|:--
processInstanceId|string|是|编排实例id

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
id|int|编排实例id
entityDataId|string|根数据id
entityTypeId|string|根数据ci
procDefId|string|编排id
procDefKey|string|编排Key
procInstKey|string|编排实例Key
status|string|状态->InProgress(执⾏中) | Completed(完成) | 其他值都是失败
taskNodeInstances|[\[\]ProcessTaskNode](#ProcessTaskNode)|绑定数据返回的数据


##### 示例：
请求： /platform/v1/process/instances/622  
正常输出：
```
{
    "status": "OK",
    "message": "Success",
    "data": {
        "id": 622,
        "procInstKey": "tZCmbG4q5HU8",
        "operator": "umadmin",
        "status": "InProgress",
        "procDefId": "sOahgazW2BYd",
        "procDefKey": "wecube1634790851797",
        "entityTypeId": "wecmdb:subsystem",
        "entityDataId": "subsystem_60b9f96d2edae48b",
        "taskNodeInstances": [
            {
                "nodeId": "SubProcess_1oc2kus",
                "nodeName": "分配数据库资源",
                "nodeType": "subProcess",
                "nodeDefId": "sOahgaTW2C2B",
                "status": "NotStarted",
                "orderedNo": "3",
                "procDefId": "sOahgazW2BYd",
                "procDefKey": "wecube1634790851797",
                "routineExpression": null,
                "taskCategory": null,
                "serviceId": null,
                "dynamicBind": null,
                "description": null,
                "previousNodeIds": [
                    "SubProcess_0wm3io2"
                ],
                "succeedingNodeIds": [
                    "SubProcess_0bjiklu"
                ],
                "procInstId": 622,
                "procInstKey": "tZCmbG4q5HU8",
                "id": 3859
            }
        ]
    }
}
```

## 数据结构：
### 1. 公共数据结构
#### <span id="ProcessObj">ProcessObj</span>
名称|类型|必选|描述
:--|:--|:--|:--
procDefId|string|是|编排id
procDefKey|string|是|编排key,同一编排在不同版本的key相同,id不同
procDefName|string|是|编排名称
procDefVersion|string|是|编排版本
rootEntity|string|是|根ci
status|string|是|状态,deployed是发布态
createdTime|string|是|创建时间

#### <span id="EntityPreview">EntityPreview</span>
名称|类型|必选|描述
:--|:--|:--|:--
processSessionId|string|是|sessionId
entityTreeNodes|[\[\]EntityTreeNode](#EntityTreeNode)|是|数据节点

#### <span id="EntityTreeNode">EntityTreeNode</span>
名称|类型|必选|描述
:--|:--|:--|:--
id|string|是|id
displayName|string|是|显示名
dataId|string|是|数据id
entityName|string|是|所属ci
packageName|string|是|所属包
previousIds|[]string|是|父数据节点
succeedingIds|[]string|是|子数据节点

#### <span id="ProcessNode">ProcessNode</span>
名称|类型|必选|描述
:--|:--|:--|:--
entityDataId|string|是|数据id
entityTypeId|string|是|数据项类型
nodeDefId|string|是|编排节点id
bound|string|是|是否绑定->Y(是) | N(否)
orderedNo|string|是|排序

#### <span id="ProcessTaskNode">ProcessTaskNode</span>
名称|类型|必选|描述
:--|:--|:--|:--
id|int|是|编排节点实例id
nodeId|string|是|节点id
nodeDefId|string|是|节点定义id
nodeName|string|是|节点名称
nodeType|string|是|节点类型
status|string|是|状态-> NotStarted(未开始)