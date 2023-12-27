## WeCube 登录 API 概览：
#### POST: /auth/v1/api/login
用户登录获取token
##### 输入参数：
参数名称|类型|必选|描述
:--|:--|:--|:--
username|string|是|用户名
password|string|是|密码

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]TokenObj](#TokenObj)|返回数据，会返回一个accessToken和一个refreshToken

##### 示例：
请求 /auth/v1/api/login  
正常输入：
```
{
  "username": "admin",
  "password": "admin"
}
```
正常输出：
```
{
    "status": "OK",
    "message": "success",
    "data": [
        {
            "expiration": "22857254307543",
            "token": "token值",
            "tokenType": "refreshToken" // refreshToken的时效较长内容较简单，它是拿来当accessToken失效时刷新token用的
        },
        {
            "expiration": "22857254307543",
            "token": "token值",
            "tokenType": "accessToken" // 主要拿这个accessToken来认证其它接口
        }
    ]
}
```

#### GET: /auth/v1/api/token
刷新token
##### 输入参数：
请求header中需要带上 Authorization: {{refreshToken的值}}

##### 输出参数：
参数名称|类型|描述
:--|:--|:--    
status|string|请求状态
message|string|返回信息
data|[\[\]TokenObj](#TokenObj)|返回数据，会返回一个accessToken和一个refreshToken

##### 示例：
请求： /auth/v1/api/token  
正常输出：
```
{
    "status": "OK",
    "message": "success",
    "data": [
        {
            "expiration": "22857254307543",
            "token": "token值",
            "tokenType": "refreshToken" // refreshToken的时效较长内容较简单，它是拿来当accessToken失效时刷新token用的
        },
        {
            "expiration": "22857254307543",
            "token": "token值",
            "tokenType": "accessToken" // 主要拿这个accessToken来认证其它接口
        }
    ]
}
```



## 数据结构：
### 1. 公共数据结构
#### <span id="TokenObj">TokenObj</span>
名称|类型|必选|描述
:--|:--|:--|:--
expiration|string|是|过期时间
token|string|是|认证token
tokenType|string|是|token类型