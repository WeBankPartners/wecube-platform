## WeCube 物料 API 概览：
#### GET: /artifacts/system-design-versions
获取系统设计列表
##### 输入参数：
无

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[SystemDesignListData](#SystemDesignListData)|返回数据

##### 示例：
请求 SystemDesignListData
正常输出：
```
{
    "code": 200,
    "status": "OK",
    "data": {
        "contents": [
            {
                "app_system_design_id": "123458",
                "application_domain": {
                    "guid": "application_domain_60b9a491b5d2c8df",
                    "key_name": "BD_\u4e1a\u52a1\u5e94\u7528\u57df"
                },
                "code": "DEMO",
                "confirm_time": "2021-06-04 09:03:37",
                "create_time": "2021-06-04 08:24:39",
                "create_user": "umadmin",
                "data_center_design": {
                    "guid": "data_center_design_60b9aa42f3507a27",
                    "key_name": "RDC"
                },
                "guid": "app_system_design_60b9e3479afe1d2c",
                "history_action": "confirm",
                "history_state_confirmed": "1",
                "history_time": "2021-06-04 09:03:37",
                "id": "18",
                "key_name": "DEMO",
                "name": "\u6f14\u793a\u7cfb\u7edf",
                "nextOperations": [
                    "Update",
                    "Delete",
                    "Confirm"
                ],
                "state": "updated_1",
                "update_time": "2021-06-04 09:03:37",
                "update_user": "umadmin"
            }
        ]
    },
    "message": "success"
}
```

#### GET: /artifacts/system-design-versions/{{appSystemDesignGuid}}
系统设计详情查询，返回三层数据，第一层是应用系统设计，第二层是子系统设计，第三层是单元设计
##### 输入参数：
url参数  
参数名称|类型|必选|描述
:--|:--|:--|:--
appSystemDesignGuid|string|是|应用系统设计guid

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]SystemDesignTreeNode](#SystemDesignTreeNode)|返回数据

##### 示例：
请求： /artifacts/system-design-versions/app_system_design_60b9e3479afe1d2c  
正常输出：
```
{
  "code": 200,
  "status": "OK",
  "data": [
    {
      "children": [
        {
          "app_system_design": "app_system_design_60b9e3479afe1d2c",
          "children": [
            {
              "code": "BROWER",
              "confirm_time": "2021-06-04 09:03:37",
              "guid": "unit_design_60b9e9540281a537",
              "key_name": "DEMO_CLIENT_BROWER",
              "name": "浏览器",
              "state": "added_1",
              "subsystem_design": "subsystem_design_60b9e93ebd782435",
              "update_time": "2021-06-04 09:03:37"
            }
          ],
          "code": "CLIENT",
          "confirm_time": "2021-06-04 09:03:37",
          "guid": "subsystem_design_60b9e93ebd782435",
          "key_name": "DEMO_CLIENT",
          "name": "客户端子系统",
          "state": "added_1",
          "update_time": "2021-06-04 09:03:37"
        }
      ],
      "code": "DEMO",
      "confirm_time": "2021-06-04 09:03:37",
      "guid": "app_system_design_60b9e3479afe1d2c",
      "key_name": "DEMO",
      "name": "演示系统",
      "state": "updated_1",
      "update_time": "2021-06-04 09:03:37"
    }
  ],
  "message": "success"
}
```

#### POST: /artifacts/unit-designs/{{unitDesignGuid}}/packages/queryNexusDirectiry
获取在线nexus物料包列表
##### 输入参数：
参数名称|类型|必选|描述
:--|:--|:--|:--
filters|[\[\]EntityQueryObj](#EntityQueryObj)|否|查询条件
paging|bool|否|是否分页


##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]PackageObj](#PackageObj)|返回数据

##### 示例：
请求 /artifacts/unit-designs/unit_design_0000000129/packages/queryNexusDirectiry
正常输入：
```
{
  "filters": [],
  "paging": false
}
```

正常输出：
```
{
    "code": 200,
    "status": "OK",
    "data": [
        {
            "name": "test_app.tar.gz",
            "downloadUrl": "http://127.0.0.1:8081/repository/apps/test_app.tar.gz",
            "md5": "b1ea5adfe4d6ed7106df1cf524d81a61"
        }
    ],
    "message": "success"
}
```

#### POST: /artifacts/packages/auto-create-deploy-package
绑定nexus物料包和物料基线
##### 输入参数：
参数名称|类型|必选|描述
:--|:--|:--|:--
nexusUrl|string|否|物料包在nexus的地址
baselinePackage|string|否|基线版本id


##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[BindDeployPackage](#BindDeployPackage)|返回数据

##### 示例：
请求 /artifacts/packages/auto-create-deploy-package
正常输入：
```
{
  "nexusUrl": "http:/xxxx/system/subsystem/artifact",
  "baselinePackage": "baseLineID"
}
```

正常输出：
```
{
  "status": "OK",
  "message": "success",
  "data": {
    "guid": "newBaseLineId"
  }
}
```

## 数据结构：
### 1. 公共数据结构
#### <span id="SystemDesignObj">SystemDesignObj</span>
名称|类型|必选|描述
:--|:--|:--|:--
guid|string|是|系统设计guid
name|string|是|系统设计显示名
key_name|string|是|系统设计编码
confirm_time|string|是|定版时间

#### <span id="SystemDesignListData">SystemDesignListData</span>
名称|类型|必选|描述
:--|:--|:--|:--
contents|[\[\]SystemDesignObj](#SystemDesignObj)|是|系统设计列表

#### <span id="SystemDesignTreeNode">SystemDesignTreeNode</span>
名称|类型|必选|描述
:--|:--|:--|:--
guid|string|是|guid
name|string|是|显示名
key_name|string|是|编码
confirm_time|string|是|确认时间
app_system_design|string|是|应用系统设计
app_system_design|string|是|子系统设计
children|[\[\]SystemDesignTreeNode](#SystemDesignTreeNode)|是|子节点

#### <span id="EntityQueryObj">EntityQueryObj</span>
名称|类型|必选|描述
:--|:--|:--|:--
attrName|string|是|属性名
op|string|是|条件
condition|object|是|条件值

#### <span id="PackageObj">PackageObj</span>
名称|类型|必选|描述
:--|:--|:--|:--
name|string|是|包名
downloadUrl|string|是|包地址
md5|string|是|md5

#### <span id="BindDeployPackage">BindDeployPackage</span>
名称|类型|必选|描述
:--|:--|:--|:--
guid|string|是|应新包的新基线ID