package models

import "time"

type ResourceServer struct {
	Id            string    `json:"id" xorm:"id"`                        // 唯一标识
	CreatedBy     string    `json:"createdBy" xorm:"created_by"`         // 创建人
	CreatedDate   time.Time `json:"createdDate" xorm:"created_date"`     // 创建时间
	Host          string    `json:"host" xorm:"host"`                    // 主机
	IsAllocated   bool      `json:"isAllocated" xorm:"is_allocated"`     // 是否分配
	LoginPassword string    `json:"loginPassword" xorm:"login_password"` // 连接密码
	LoginUsername string    `json:"loginUsername" xorm:"login_username"` // 连接用户名
	Name          string    `json:"name" xorm:"name"`                    // 名称
	Port          string    `json:"port" xorm:"port"`                    // 端口
	Purpose       string    `json:"purpose" xorm:"purpose"`              // 描述
	Status        string    `json:"status" xorm:"status"`                // 状态,是否启用->inactive | active
	Type          string    `json:"type" xorm:"type"`                    // 资源类型(docker,mysql,s3)
	UpdatedBy     string    `json:"updatedBy" xorm:"updated_by"`         // 更新人
	UpdatedDate   time.Time `json:"updatedDate" xorm:"updated_date"`     // 更新时间
	LoginMode     string    `json:"loginMode" xorm:"login_mode"`         // 登录模式
}

type ResourceItem struct {
	Id                   string    `json:"id" xorm:"id"`                                      // 唯一标识
	ResourceServerId     string    `json:"resourceServerId" xorm:"resource_server_id"`        // 关联资源
	AdditionalProperties string    `json:"additionalProperties" xorm:"additional_properties"` // 连接参数
	CreatedBy            string    `json:"createdBy" xorm:"created_by"`                       // 创建人
	CreatedDate          time.Time `json:"createdDate" xorm:"created_date"`                   // 创建时间
	IsAllocated          bool      `json:"isAllocated" xorm:"is_allocated"`                   // 是否分配
	Name                 string    `json:"name" xorm:"name"`                                  // 名称
	Purpose              string    `json:"purpose" xorm:"purpose"`                            // 描述
	Status               string    `json:"status" xorm:"status"`                              // 状态
	Type                 string    `json:"type" xorm:"type"`                                  // 类型
	UpdatedBy            string    `json:"updatedBy" xorm:"updated_by"`                       // 更新人
	UpdatedDate          time.Time `json:"updatedDate" xorm:"updated_date"`                   // 更新时间
}

type ResourceServerListPageData struct {
	PageInfo *PageInfo         `json:"pageInfo"` // 分页信息
	Contents []*ResourceServer `json:"contents"` // 列表内容
}

type ResourceItemListPageData struct {
	PageInfo *PageInfo       `json:"pageInfo"` // 分页信息
	Contents []*ResourceItem `json:"contents"` // 列表内容
}

type MysqlResourceItemProperties struct {
	Username string `json:"username"`
	Password string `json:"password"`
}
