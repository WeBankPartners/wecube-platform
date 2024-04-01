package models

type WeLicense struct {
	Plugin      string `json:"plugin"`      // 插件名称
	Lpk         string `json:"lpk"`         // 公钥
	Data        string `json:"data"`        // 加密数据
	Signature   string `json:"signature"`   // 数据签名
	Description string `json:"description"` // License说明
}

type PluginCertification struct {
	Id          string `json:"id" xorm:"id"`                    // 唯一标识
	Plugin      string `json:"plugin" xorm:"plugin"`            // 插件名称
	Lpk         string `json:"lpk" xorm:"lpk"`                  // 公钥
	EncryptData string `json:"encryptData" xorm:"encrypt_data"` // 加密数据
	Signature   string `json:"signature" xorm:"signature"`      // 数据签名
	Description string `json:"description" xorm:"description"`  // License说明
	CreatedTime string `json:"createdTime" xorm:"created_time"` // 创建时间
	CreatedBy   string `json:"createdBy" xorm:"created_by"`     // 创建人
	UpdatedTime string `json:"updatedTime" xorm:"updated_time"` // 更新时间
	UpdatedBy   string `json:"updatedBy" xorm:"updated_by"`     // 更新人
}
