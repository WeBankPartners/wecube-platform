# 多因素登录（MFA）设计方案

## 一、需求概述

实现基于 TOTP（Time-based One-Time Password）的多因素登录功能，增强系统安全性。用户首次登录需要绑定 Authenticator 应用，后续登录需要输入动态验证码。

## 二、技术方案

### 2.1 依赖库
使用 `github.com/pquerna/otp` 库实现 TOTP 功能（参考 demo.go）

### 2.2 系统变量配置

新增以下系统变量（存储在 `system_variables` 表）：

1. **MFA_ENABLED** (全局)
   - 作用：控制是否启用多因素登录
   - 类型：字符串，值为 "true" 或 "false"
   - 默认值：false
   - 作用域：global

2. **MFA_ISSUER_NAME** (全局)
   - 作用：二维码中的应用名称（对应 demo.go 中的 "WeCube Login"）
   - 类型：字符串
   - 默认值：WeCube Login
   - 作用域：global

3. **MFA_TOTP_PERIOD** (全局)
   - 作用：TOTP 验证码刷新周期（单位：秒）
   - 类型：整数（字符串形式存储）
   - 默认值：60（1分钟）
   - 作用域：global

### 2.3 数据库变更

#### 2.3.1 表结构变更
在 `auth_sys_user` 表中新增字段：

```sql
ALTER TABLE `auth_sys_user` 
ADD COLUMN `mfa_secret` VARCHAR(255) NULL DEFAULT NULL COMMENT 'MFA密钥，用于生成TOTP二维码' AFTER `auth_src`;
```

#### 2.3.2 实体类变更
- `platform-auth-server/model/entity.go` 中的 `SysUserEntity` 结构体新增 `MfaSecret` 字段

## 三、接口设计

### 3.1 登录接口（改造）

**接口路径**：`POST /auth/v1/api/login`

**改造逻辑**：

1. **原有流程保持不变**：验证用户名密码 (UM登录不管)
2. **新增 MFA 判断逻辑**：
   - 登录成功后，查询系统变量 `MFA_ENABLED`
   - 如果未启用 MFA（MFA_ENABLED != "true"），直接返回 token（兼容旧逻辑）
   - 如果启用了 MFA：
     - 查询用户是否已绑定 secret（`mfa_secret` 字段是否为空）
     - **如果未绑定**：
       - 生成 TOTP secret
       - 生成二维码 URL
       - 保存 secret 到数据库
       - 返回特殊响应（不返回 token，返回二维码信息）
     - **如果已绑定**：
       - 返回特殊响应（不返回 token，提示需要输入 TOTP Code）

**新的响应结构**（当启用 MFA 时）：
- **未绑定用户**（需要显示二维码）：
```json
{
  "status": "OK",
  
  "data": {
    "qrCodeUrl": "otpauth://totp/WeCube%20Login:username?secret=...&issuer=WeCube%20Login",
    "tempToken": "临时token，用于后续MFA验证，有效期5分钟"
  }
}
```

- **已绑定用户**（需要输入验证码）：
```json
{
  "status": "OK",
  "data": {
    "needMfaCode": true,
    "tempToken": "临时token，用于后续MFA验证，有效期5分钟"
  }
}
```

**说明**：
- `tempToken`：临时访问令牌，用于验证用户已通过第一步登录验证
- 临时 token 有效期：5 分钟（可配置）
- 临时 token 仅用于 `/auth/v1/api/mfa/verify` 接口验证，不能用于其他业务接口

**原有响应结构**（未启用 MFA 或 MFA 验证通过后）：
```json
{
  "status": "OK",
  "data": [
    {
      "token": "...",
      "tokenType": "accessToken",
      "expiration": "..."
    },
    {
      "token": "...",
      "tokenType": "refreshToken",
      "expiration": "..."
    }
  ]
}
```

### 3.2 验证 TOTP Code 接口（新增）

**接口路径**：`POST /auth/v1/api/mfa/verify`

**功能**：验证用户输入的 TOTP 动态码

**请求参数**：
```json
{
  "username": "username",
  "code": "123456",
  "tempToken": "第一步登录返回的临时token"
}
```

**响应结构**：
- 验证成功：返回正式 token（与原有登录成功响应一致）
- 验证失败：返回错误信息

**安全机制**：
- **临时 token 验证**：必须携带第一步登录返回的 `tempToken`，验证通过后才能进行 TOTP 验证
- **防止直接调用**：未通过第一步登录的用户无法直接调用此接口
- **临时 token 有效期**：5 分钟，过期后需要重新登录
- **一次性使用**：临时 token 验证成功后即失效，不能重复使用

**实现位置**：
- `platform-auth-server/api/api_auth.go` 新增 `VerifyMfaCode` 方法
- `platform-auth-server/service/auth_service.go` 新增 `VerifyMfaCode` 服务方法

## 四、登录流程设计

### 4.1 前端流程

```
1. 用户输入用户名密码，调用 POST /auth/v1/api/login
2. 根据响应判断：
   ├─> 如果返回 token（data 是数组）：登录成功（未启用 MFA）
   ├─> 如果返回 qrCodeUrl（data.qrCodeUrl 存在）：
   │   ├─> 保存 tempToken（data.tempToken）
   │   └─> 显示二维码，要求用户扫描并输入验证码
   │       └─> 用户输入验证码后，调用 POST /auth/v1/api/mfa/verify
   │           ├─> 请求参数：{ username, code, tempToken }
   │           ├─> 成功：返回正式 token，登录完成
   │           └─> 失败：提示错误，允许重试（tempToken 仍有效，可重试）
   └─> 如果返回 needMfaCode（data.needMfaCode 为 true）：
       ├─> 保存 tempToken（data.tempToken）
       └─> 直接显示输入框，要求用户输入验证码
           └─> 用户输入验证码后，调用 POST /auth/v1/api/mfa/verify
               ├─> 请求参数：{ username, code, tempToken }
               ├─> 成功：返回正式 token，登录完成
               └─> 失败：提示错误，允许重试（tempToken 仍有效，可重试）
```

### 4.2 后端流程

#### 4.2.1 Login 方法改造流程

```
Login(credential)
  ├─> 验证用户名密码（原有逻辑）
  ├─> 查询系统变量 MFA_ENABLED
  ├─> 如果 MFA_ENABLED != "true"
  │   └─> 直接返回正式 token（原有逻辑）
  └─> 如果 MFA_ENABLED == "true"
      ├─> 生成临时 token（有效期5分钟）
      ├─> 查询用户 mfa_secret 字段
      ├─> 如果 secret 为空（未绑定）
      │   ├─> 生成 TOTP secret
      │   ├─> 生成二维码 URL
      │   ├─> 保存 secret 到数据库
      │   └─> 返回 { qrCodeUrl: "...", tempToken: "..." }（不返回正式 token）
      └─> 如果 secret 不为空（已绑定）
          └─> 返回 { needMfaCode: true, tempToken: "..." }（不返回正式 token）
```

#### 4.2.2 VerifyMfaCode 方法流程

```
VerifyMfaCode(username, code, tempToken)
  ├─> 验证临时 token（validateTempToken）
  │   ├─> 验证 token 签名
  │   ├─> 验证 token 类型为 "mfa_temp"
  │   ├─> 验证 token 未过期
  │   └─> 验证 token 中的用户名与请求参数一致
  ├─> 如果临时 token 验证失败
  │   └─> 返回错误："请先完成第一步登录验证"
  ├─> 查询用户的 mfa_secret
  ├─> 使用 totp.Validate(code, secret) 验证 TOTP Code
  ├─> 如果 TOTP 验证成功
  │   ├─> 生成正式 token（accessToken + refreshToken）
  │   └─> 返回正式 token（临时 token 自动失效）
  └─> 如果 TOTP 验证失败
      └─> 返回错误信息："验证码错误"
```

## 五、实现细节

### 5.1 生成 TOTP Secret 和二维码

参考 demo.go 的实现：

```go
// 从系统变量读取配置
issuerName := getSystemVariable("MFA_ISSUER_NAME", "WeCube Login")
period := getSystemVariableInt("MFA_TOTP_PERIOD", 60) // 默认60秒

key, err := totp.Generate(totp.GenerateOpts{
    Issuer:      issuerName,
    AccountName: username,
    Period:      uint(period), // 从系统变量 MFA_TOTP_PERIOD 读取，默认60秒
    Digits:      otp.DigitsSix,
    Algorithm:   otp.AlgorithmSHA1,
})
```

### 5.2 验证 TOTP Code

```go
valid := totp.Validate(code, secret)
```

### 5.3 系统变量查询

在 auth-server 中通过调用 platform-core 的接口查询系统变量：
- 使用 `api_platform.QuerySystemVariables` 方法（已存在）

### 5.4 临时 Token 机制

**目的**：防止用户跳过第一步登录验证，直接调用 MFA 验证接口。

**实现方案**：

1. **生成临时 Token**（在 Login 方法中）：
   - 当启用 MFA 且用户通过用户名密码验证后
   - 生成一个临时 JWT token，包含以下信息：
     - `subject`: 用户名
     - `type`: "mfa_temp"（标识为临时 token）
     - `expiresAt`: 当前时间 + 5 分钟
   - 将临时 token 返回给前端

2. **验证临时 Token**（在 VerifyMfaCode 方法中）：
   - 接收前端传来的 `tempToken`
   - 解析并验证临时 token：
     - 验证 token 签名
     - 验证 token 类型为 "mfa_temp"
     - 验证 token 未过期
     - 验证 token 中的用户名与请求参数一致
   - 验证通过后，继续 TOTP 验证流程
   - 验证成功后，临时 token 即失效（可选：可加入黑名单防止重复使用）

3. **生成正式 Token**：
   - TOTP 验证成功后，生成正式的 accessToken 和 refreshToken
   - 返回给前端，完成登录流程

**临时 Token 生成示例**：
```go
// 生成临时 token
tempToken, err := buildTempToken(username, 5*time.Minute) // 5分钟有效期

func buildTempToken(username string, duration time.Duration) (string, error) {
    issueAt := time.Now().UTC().Unix()
    exp := time.Now().Add(duration).UTC().Unix()
    token := jwt.NewWithClaims(jwt.SigningMethodHS512, model.AuthClaims{
        Subject:   username,
        IssuedAt:  issueAt,
        ExpiresAt: exp,
        Type:      "mfa_temp", // 标识为临时 token
    })
    return token.SignedString(model.Config.Auth.SigningKeyBytes)
}
```

**临时 Token 验证示例**：
```go
// 验证临时 token
func validateTempToken(tempToken, username string) error {
    jwtToken, err := jwt.ParseWithClaims(tempToken, &model.AuthClaims{}, func(token *jwt.Token) (interface{}, error) {
        return model.Config.Auth.SigningKeyBytes, nil
    })
    if err != nil {
        return errors.New("invalid temp token")
    }
    
    claim, ok := jwtToken.Claims.(*model.AuthClaims)
    if !ok || !jwtToken.Valid {
        return errors.New("invalid temp token")
    }
    
    // 验证 token 类型
    if claim.Type != "mfa_temp" {
        return errors.New("invalid token type")
    }
    
    // 验证用户名
    if claim.Subject != username {
        return errors.New("username mismatch")
    }
    
    // 验证未过期（JWT 库会自动验证）
    return nil
}
```

## 六、安全考虑

1. **Secret 存储**：secret 存储在数据库中，建议加密存储（可选,采用base64加密）
2. **验证码有效期**：TOTP 验证码刷新周期可通过系统变量 `MFA_TOTP_PERIOD` 配置（单位：秒），默认 60 秒（1分钟）
3. **重试限制**：验证失败可设置重试次数限制（可选，当前登录没有失败次数限制，暂时不限制）
4. **临时 Token 机制**：
   - 第一步登录成功后生成临时 token（有效期 5 分钟）
   - 临时 token 仅用于 MFA 验证接口，防止跳过登录直接验证
   - 临时 token 验证成功后即失效，不能重复使用
   - 临时 token 过期后需要重新登录
5. **时间同步**：确保服务器时间准确，TOTP 对时间敏感
