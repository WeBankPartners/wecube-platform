package models

import "time"

type ResourceServer struct {
	Id            string    `json:"Id" xorm:"id"`                        // 唯一标识
	CreatedBy     string    `json:"CreatedBy" xorm:"created_by"`         // 创建人
	CreatedDate   time.Time `json:"CreatedDate" xorm:"created_date"`     // 创建时间
	Host          string    `json:"Host" xorm:"host"`                    // 主机
	IsAllocated   bool      `json:"IsAllocated" xorm:"is_allocated"`     // 是否分配
	LoginPassword string    `json:"LoginPassword" xorm:"login_password"` // 连接密码
	LoginUsername string    `json:"LoginUsername" xorm:"login_username"` // 连接用户名
	Name          string    `json:"Name" xorm:"name"`                    // 名称
	Port          string    `json:"Port" xorm:"port"`                    // 端口
	Purpose       string    `json:"Purpose" xorm:"purpose"`              // 描述
	Status        bool      `json:"Status" xorm:"status"`                // 状态是否启用
	Type          string    `json:"Type" xorm:"type"`                    // 资源类型(docker,mysql,s3)
	UpdatedBy     string    `json:"UpdatedBy" xorm:"updated_by"`         // 更新人
	UpdatedDate   time.Time `json:"UpdatedDate" xorm:"updated_date"`     // 更新时间
	LoginMode     string    `json:"LoginMode" xorm:"login_mode"`         // 登录模式
}

type ResourceItem struct {
	Id                   string    `json:"Id" xorm:"id"`                                      // 唯一标识
	ResourceServerId     string    `json:"ResourceServerId" xorm:"resource_server_id"`        // 关联资源
	AdditionalProperties string    `json:"AdditionalProperties" xorm:"additional_properties"` // 连接参数
	CreatedBy            string    `json:"CreatedBy" xorm:"created_by"`                       // 创建人
	CreatedDate          time.Time `json:"CreatedDate" xorm:"created_date"`                   // 创建时间
	IsAllocated          bool      `json:"IsAllocated" xorm:"is_allocated"`                   // 是否分配
	Name                 string    `json:"Name" xorm:"name"`                                  // 名称
	Purpose              string    `json:"Purpose" xorm:"purpose"`                            // 描述
	Status               int8      `json:"Status" xorm:"status"`                              // 状态
	Type                 string    `json:"Type" xorm:"type"`                                  // 类型
	UpdatedBy            string    `json:"UpdatedBy" xorm:"updated_by"`                       // 更新人
	UpdatedDate          time.Time `json:"UpdatedDate" xorm:"updated_date"`                   // 更新时间
}
