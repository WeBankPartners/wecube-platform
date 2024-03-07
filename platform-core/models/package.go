package models

type OnlinePackage struct {
	BucketName string `json:"bucketName"`
	KeyName    string `json:"keyName"`
}

type PullOnliePackageRequest struct {
	KeyName string `json:"keyName"`
}

type PullOnliePackageResponse struct {
	ErrorMessage string `json:"errorMessage"`
	KeyName      string `json:"keyName"`
	RequestId    string `json:"requestId"`
	State        string `json:"state"`
}

type PluginArtifactPullReq struct {
	Id         string `json:"id" xorm:"id"`                  // 唯一标识
	BucketName string `json:"bucketName" xorm:"bucket_name"` // 预留
	ErrMsg     string `json:"errMsg" xorm:"err_msg"`         // State=faulted时，记录错误信息
	KeyName    string `json:"keyName" xorm:"key_name"`       // 插件包文件名
	PkgId      string `json:"pkgId" xorm:"pkg_id"`           // 插件包ID
	State      string `json:"state" xorm:"state"`            // 状态：InProgress，Completed，Faulted
	Rev        int    `json:"rev" xorm:"rev"`                // 数据版本号，从0开始
	TotalSize  int    `json:"totalSize" xorm:"total_size"`   // 包大小

	CreatedTime string `json:"createdTime" xorm:"created_time"` // 创建时间
	CreatedBy   string `json:"createdBy" xorm:"created_by"`     // 创建人
	UpdatedTime string `json:"updatedTime" xorm:"updated_time"` // 更新时间
	UpdatedBy   string `json:"updatedBy" xorm:"updated_by"`     // 更新人
}
