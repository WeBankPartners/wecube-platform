package models

import (
	"encoding/xml"
	"time"
)

const (
	PluginStatusUnRegistered   = "unregistered"
	PluginStatusRegistered     = "registered"
	PluginStatusDecommissioned = "decommissioned"
	PluginEditionCommunity     = "community"
	PluginEditionEnterprise    = "enterprise"
	PluginParamTypeInput       = "INPUT"
	PluginParamTypeOutput      = "OUTPUT"
)

type PluginPackages struct {
	Id                string    `json:"id" xorm:"id"`                                 // 唯一标识
	Name              string    `json:"name" xorm:"name"`                             // 显示名
	Version           string    `json:"version" xorm:"version"`                       // 版本
	Status            string    `json:"status" xorm:"status"`                         // 状态->0(unregistered已上传未注册态)|1(registered注册态)|2(decommissioned注销态)
	UploadTimestamp   time.Time `json:"uploadTimestamp" xorm:"upload_timestamp"`      // 上传时间
	UiPackageIncluded bool      `json:"uiPackageIncluded" xorm:"ui_package_included"` // 是否有ui->0(无)|1(有)
	Edition           string    `json:"edition" xorm:"edition"`                       // 发行版本->0(community社区版)|1(enterprise企业版)
}

type PluginInstances struct {
	Id                            string `json:"id" xorm:"id"`                                                           // 唯一标识
	Host                          string `json:"host" xorm:"host"`                                                       // 主机ip
	ContainerName                 string `json:"containerName" xorm:"container_name"`                                    // 容器名
	Port                          int    `json:"port" xorm:"port"`                                                       // 服务端口
	ContainerStatus               string `json:"containerStatus" xorm:"container_status"`                                // 容器状态
	PackageId                     string `json:"packageId" xorm:"package_id"`                                            // 插件
	DockerInstanceResourceId      string `json:"dockerInstanceResourceId" xorm:"docker_instance_resource_id"`            // 容器实例id
	InstanceName                  string `json:"instanceName" xorm:"instance_name"`                                      // 容器实例名
	PluginMysqlInstanceResourceId string `json:"pluginMysqlInstanceResourceId" xorm:"plugin_mysql_instance_resource_id"` // 数据库实例id
	S3bucketResourceId            string `json:"s3bucketResourceId" xorm:"s3bucket_resource_id"`                         // s3资源id
}

type PluginPackageRuntimeResourcesDocker struct {
	Id              string `json:"id" xorm:"id"`                             // 唯一标识
	PluginPackageId string `json:"pluginPackageId" xorm:"plugin_package_id"` // 插件
	ImageName       string `json:"imageName" xorm:"image_name"`              // 镜像名
	ContainerName   string `json:"containerName" xorm:"container_name"`      // 容器名
	PortBindings    string `json:"portBindings" xorm:"port_bindings"`        // 端口信息
	VolumeBindings  string `json:"volumeBindings" xorm:"volume_bindings"`    // 目录映射
	EnvVariables    string `json:"envVariables" xorm:"env_variables"`        // 容器环境变量
}

type PluginPackageRuntimeResourcesMysql struct {
	Id              string `json:"id" xorm:"id"`                             // 唯一标识
	PluginPackageId string `json:"pluginPackageId" xorm:"plugin_package_id"` // 插件
	SchemaName      string `json:"schemaName" xorm:"schema_name"`            // 数据库名
	InitFileName    string `json:"initFileName" xorm:"init_file_name"`       // 初始化脚本
	UpgradeFileName string `json:"upgradeFileName" xorm:"upgrade_file_name"` // 升级脚本
}

type PluginPackageRuntimeResourcesS3 struct {
	Id                   string `json:"id" xorm:"id"`                                      // 唯一标识
	PluginPackageId      string `json:"pluginPackageId" xorm:"plugin_package_id"`          // 插件
	BucketName           string `json:"bucketName" xorm:"bucket_name"`                     // 桶名
	AdditionalProperties string `json:"additionalProperties" xorm:"additional_properties"` // 自动上传文件
}

type PluginMysqlInstances struct {
	Id              string    `json:"id" xorm:"id"`                             // 唯一标识
	Password        string    `json:"password" xorm:"password"`                 // 密码
	PluginPackageId string    `json:"pluginPackageId" xorm:"plugin_package_id"` // 插件
	ResourceItemId  string    `json:"resourceItemId" xorm:"resource_item_id"`   // 资源实例id
	SchemaName      string    `json:"schemaName" xorm:"schema_name"`            // 数据库名
	Status          bool      `json:"status" xorm:"status"`                     // 状态->0(inactive)|1(active)
	Username        string    `json:"username" xorm:"username"`                 // 用户名
	PreVersion      string    `json:"preVersion" xorm:"pre_version"`            // 插件版本
	CreatedTime     time.Time `json:"createdTime" xorm:"created_time"`          // 创建时间
	UpdatedTime     time.Time `json:"updatedTime" xorm:"updated_time"`          // 更新时间
}

type PluginPackageAuthorities struct {
	Id              string `json:"id" xorm:"id"`                             // 唯一标识
	PluginPackageId string `json:"pluginPackageId" xorm:"plugin_package_id"` // 插件
	RoleName        string `json:"roleName" xorm:"role_name"`                // 角色
	MenuCode        string `json:"menuCode" xorm:"menu_code"`                // 菜单编码
}

type PluginPackageDependencies struct {
	Id                       string `json:"id" xorm:"id"`                                               // 唯一标识
	PluginPackageId          string `json:"pluginPackageId" xorm:"plugin_package_id"`                   // 插件
	DependencyPackageName    string `json:"dependencyPackageName" xorm:"dependency_package_name"`       // 依赖包名
	DependencyPackageVersion string `json:"dependencyPackageVersion" xorm:"dependency_package_version"` // 依赖包版本
}

type PluginPackageDepObj struct {
	PackageName  string                 `json:"packageName"`
	Version      string                 `json:"version"`
	Dependencies []*PluginPackageDepObj `json:"dependencies"`
}

type PluginPackageResourceFiles struct {
	Id              string `json:"id" xorm:"id"`                             // 唯一标识
	PluginPackageId string `json:"pluginPackageId" xorm:"plugin_package_id"` // 插件
	PackageName     string `json:"packageName" xorm:"package_name"`          // 插件包名
	PackageVersion  string `json:"packageVersion" xorm:"package_version"`    // 插件版本
	Source          string `json:"source" xorm:"source"`                     // 压缩文件
	RelatedPath     string `json:"relatedPath" xorm:"related_path"`          // 静态文件路径
}

type PluginPackageMenus struct {
	Id               string `json:"id" xorm:"id"`                               // 唯一标识
	PluginPackageId  string `json:"pluginPackageId" xorm:"plugin_package_id"`   // 插件
	Code             string `json:"code" xorm:"code"`                           // 编码
	Category         string `json:"category" xorm:"category"`                   // 目录
	DisplayName      string `json:"displayName" xorm:"display_name"`            // 英文显示名
	LocalDisplayName string `json:"localDisplayName" xorm:"local_display_name"` // 本地语言显示名
	MenuOrder        int    `json:"menuOrder" xorm:"menu_order"`                // 菜单排序
	Path             string `json:"path" xorm:"path"`                           // 前端请求路径
	Active           bool   `json:"active" xorm:"active"`                       // 是否启用->0(不启用)|1(启用)
	Source           string `json:"source" xorm:"-"`
}

type PluginRuntimeResourceData struct {
	Docker []*PluginPackageRuntimeResourcesDocker `json:"docker"`
	Mysql  []*PluginPackageRuntimeResourcesMysql  `json:"mysql"`
	S3     []*PluginPackageRuntimeResourcesS3     `json:"s3"`
}

type RegisterXML struct {
	XMLName             xml.Name `xml:"package"`
	Text                string   `xml:",chardata"`
	Name                string   `xml:"name,attr"`
	Version             string   `xml:"version,attr"`
	PackageDependencies struct {
		Text              string `xml:",chardata"`
		PackageDependency []struct {
			Text    string `xml:",chardata"`
			Name    string `xml:"name,attr"`
			Version string `xml:"version,attr"`
		} `xml:"packageDependency"`
	} `xml:"packageDependencies"`
	Menus struct {
		Text string `xml:",chardata"`
		Menu []struct {
			Text             string `xml:",chardata"`
			Code             string `xml:"code,attr"`
			Cat              string `xml:"cat,attr"`
			DisplayName      string `xml:"displayName,attr"`
			LocalDisplayName string `xml:"localDisplayName,attr"`
		} `xml:"menu"`
	} `xml:"menus"`
	DataModel struct {
		Text   string `xml:",chardata"`
		Entity []struct {
			Text        string `xml:",chardata"`
			Name        string `xml:"name,attr"`
			DisplayName string `xml:"displayName,attr"`
			Description string `xml:"description,attr"`
			Attribute   []struct {
				Text        string `xml:",chardata"`
				Name        string `xml:"name,attr"`
				Datatype    string `xml:"datatype,attr"`
				Description string `xml:"description,attr"`
			} `xml:"attribute"`
		} `xml:"entity"`
	} `xml:"dataModel"`
	SystemParameters struct {
		Text            string `xml:",chardata"`
		SystemParameter []struct {
			Text         string `xml:",chardata"`
			Name         string `xml:"name,attr"`
			ScopeType    string `xml:"scopeType,attr"`
			DefaultValue string `xml:"defaultValue,attr"`
		} `xml:"systemParameter"`
	} `xml:"systemParameters"`
	Authorities struct {
		Text      string `xml:",chardata"`
		Authority struct {
			Text           string `xml:",chardata"`
			SystemRoleName string `xml:"systemRoleName,attr"`
			Menu           []struct {
				Text string `xml:",chardata"`
				Code string `xml:"code,attr"`
			} `xml:"menu"`
		} `xml:"authority"`
	} `xml:"authorities"`
	ResourceDependencies struct {
		Text   string `xml:",chardata"`
		Docker struct {
			Text           string `xml:",chardata"`
			ImageName      string `xml:"imageName,attr"`
			ContainerName  string `xml:"containerName,attr"`
			PortBindings   string `xml:"portBindings,attr"`
			VolumeBindings string `xml:"volumeBindings,attr"`
			EnvVariables   string `xml:"envVariables,attr"`
		} `xml:"docker"`
		Mysql struct {
			Text            string `xml:",chardata"`
			Schema          string `xml:"schema,attr"`
			InitFileName    string `xml:"initFileName,attr"`
			UpgradeFileName string `xml:"upgradeFileName,attr"`
		} `xml:"mysql"`
		S3 struct {
			Text       string `xml:",chardata"`
			BucketName string `xml:"bucketName,attr"`
		} `xml:"s3"`
	} `xml:"resourceDependencies"`
	Plugins struct {
		Text   string `xml:",chardata"`
		Plugin []struct {
			Text                   string `xml:",chardata"`
			Name                   string `xml:"name,attr"`
			TargetPackage          string `xml:"targetPackage,attr"`
			TargetEntity           string `xml:"targetEntity,attr"`
			RegisterName           string `xml:"registerName,attr"`
			TargetEntityFilterRule string `xml:"targetEntityFilterRule,attr"`
			Interface              []struct {
				Text              string `xml:",chardata"`
				Action            string `xml:"action,attr"`
				Path              string `xml:"path,attr"`
				FilterRule        string `xml:"filterRule,attr"`
				HttpMethod        string `xml:"httpMethod,attr"`
				IsAsyncProcessing string `xml:"isAsyncProcessing,attr"`
				Type              string `xml:"type,attr"`
				Description       string `xml:"description,attr"`
				InputParameters   struct {
					Text      string `xml:",chardata"`
					Parameter []struct {
						Text                      string `xml:",chardata"`
						Datatype                  string `xml:"datatype,attr"`
						Required                  string `xml:"required,attr"`
						SensitiveData             string `xml:"sensitiveData,attr"`
						MappingType               string `xml:"mappingType,attr"`
						MappingEntityExpression   string `xml:"mappingEntityExpression,attr"`
						MappingSystemVariableName string `xml:"mappingSystemVariableName,attr"`
						Multiple                  string `xml:"multiple,attr"`
						Description               string `xml:"description,attr"`
						MappingVal                string `xml:"mappingVal,attr"`
						RefObjectName             string `xml:"refObjectName,attr"`
					} `xml:"parameter"`
				} `xml:"inputParameters"`
				OutputParameters struct {
					Text      string `xml:",chardata"`
					Parameter []struct {
						Text                      string `xml:",chardata"`
						Datatype                  string `xml:"datatype,attr"`
						Required                  string `xml:"required,attr"`
						SensitiveData             string `xml:"sensitiveData,attr"`
						MappingType               string `xml:"mappingType,attr"`
						MappingEntityExpression   string `xml:"mappingEntityExpression,attr"`
						MappingSystemVariableName string `xml:"mappingSystemVariableName,attr"`
						Multiple                  string `xml:"multiple,attr"`
						Description               string `xml:"description,attr"`
						MappingVal                string `xml:"mappingVal,attr"`
						RefObjectName             string `xml:"refObjectName,attr"`
					} `xml:"parameter"`
				} `xml:"outputParameters"`
			} `xml:"interface"`
		} `xml:"plugin"`
	} `xml:"plugins"`
}

type PluginConfigs struct {
	Id                     string `json:"id" xorm:"id"`                                            // 唯一标识
	PluginPackageId        string `json:"pluginPackageId" xorm:"plugin_package_id"`                // 插件
	Name                   string `json:"name" xorm:"name"`                                        // 服务类型名称
	TargetPackage          string `json:"targetPackage" xorm:"target_package"`                     // 目标类型包
	TargetEntity           string `json:"targetEntity" xorm:"target_entity"`                       // 目标类型项
	TargetEntityFilterRule string `json:"targetEntityFilterRule" xorm:"target_entity_filter_rule"` // 目标类型过滤规则
	RegisterName           string `json:"registerName" xorm:"register_name"`                       // 服务注册名
	Status                 string `json:"status" xorm:"status"`                                    // 状态
}

type PluginConfigInterfaces struct {
	Id                 string `json:"id" xorm:"id"`                                   // 唯一标识
	PluginConfigId     string `json:"pluginConfigId" xorm:"plugin_config_id"`         // 插件服务
	Action             string `json:"action" xorm:"action"`                           // 接口
	ServiceName        string `json:"serviceName" xorm:"service_name"`                // 服务名
	ServiceDisplayName string `json:"serviceDisplayName" xorm:"service_display_name"` // 服务显示名
	Path               string `json:"path" xorm:"path"`                               // 插件接口uri
	HttpMethod         string `json:"httpMethod" xorm:"http_method"`                  // http请求方法
	IsAsyncProcessing  bool   `json:"isAsyncProcessing" xorm:"is_async_processing"`   // 是否同步
	Type               string `json:"type" xorm:"type"`                               // 服务类型->approval(审批),execution(执行),dynamicform(动态表单)
	FilterRule         string `json:"filterRule" xorm:"filter_rule"`                  // 服务过滤规则
	Description        string `json:"description" xorm:"description"`                 // 描述
}

type PluginConfigInterfaceParameters struct {
	Id                        string `json:"id" xorm:"id"`                                                  // 唯一标识
	PluginConfigInterfaceId   string `json:"pluginConfigInterfaceId" xorm:"plugin_config_interface_id"`     // 服务接口
	Type                      string `json:"type" xorm:"type"`                                              // 类型->input(输入),output(输出)
	Name                      string `json:"name" xorm:"name"`                                              // 接口属性名
	DataType                  string `json:"dataType" xorm:"data_type"`                                     // 属性数据类型
	MappingType               string `json:"mappingType" xorm:"mapping_type"`                               // 数据来源
	MappingEntityExpression   string `json:"mappingEntityExpression" xorm:"mapping_entity_expression"`      // entity表达式
	MappingSystemVariableName string `json:"mappingSystemVariableName" xorm:"mapping_system_variable_name"` // 系统参数
	Required                  bool   `json:"required" xorm:"required"`                                      // 是否必填
	SensitiveData             bool   `json:"sensitiveData" xorm:"sensitive_data"`                           // 是否敏感
	Description               string `json:"description" xorm:"description"`                                // 描述
	MappingVal                string `json:"mappingVal" xorm:"mapping_val"`                                 // 静态值
	Multiple                  bool   `json:"multiple" xorm:"multiple"`                                      // 是否数组
	RefObjectName             string `json:"refObjectName" xorm:"ref_object_name"`                          // 关联对象名
}

type PluginPackageDataModel struct {
	Id           string    `json:"id" xorm:"id"`                      // 唯一标识
	Version      int       `json:"version" xorm:"version"`            // 版本
	PackageName  string    `json:"packageName" xorm:"package_name"`   // 包名
	IsDynamic    bool      `json:"isDynamic" xorm:"is_dynamic"`       // 是否动态
	UpdatePath   string    `json:"updatePath" xorm:"update_path"`     // 请求路径
	UpdateMethod string    `json:"updateMethod" xorm:"update_method"` // 请求方法
	UpdateSource string    `json:"updateSource" xorm:"update_source"` // 来源
	UpdateTime   time.Time `json:"updateTime" xorm:"update_time"`     // 更新时间
}

type PluginPackageEntities struct {
	Id               string `json:"id" xorm:"id"`                               // 唯一标识
	DataModelId      string `json:"dataModelId" xorm:"data_model_id"`           // 所属数据模型
	DataModelVersion int    `json:"dataModelVersion" xorm:"data_model_version"` // 版本
	PackageName      string `json:"packageName" xorm:"package_name"`            // 包名
	Name             string `json:"name" xorm:"name"`                           // 模型名
	DisplayName      string `json:"displayName" xorm:"display_name"`            // 显示名
	Description      string `json:"description" xorm:"description"`             // 描述
}

type PluginPackageAttributes struct {
	Id          string    `json:"id" xorm:"id"`                    // 唯一标识
	EntityId    string    `json:"entityId" xorm:"entity_id"`       // 所属数据模型ci项
	ReferenceId string    `json:"referenceId" xorm:"reference_id"` // 关联数据模型
	Name        string    `json:"name" xorm:"name"`                // 属性名
	Description string    `json:"description" xorm:"description"`  // 描述
	DataType    string    `json:"dataType" xorm:"data_type"`       // 属性数据类型
	RefPackage  string    `json:"refPackage" xorm:"ref_package"`   // 关联包
	RefEntity   string    `json:"refEntity" xorm:"ref_entity"`     // 关联ci项
	RefAttr     string    `json:"refAttr" xorm:"ref_attr"`         // 关联属性
	Mandatory   bool      `json:"mandatory" xorm:"mandatory"`      // 是否必填
	Multiple    bool      `json:"multiple" xorm:"multiple"`        // 是否数组
	CreatedTime time.Time `json:"createdTime" xorm:"created_time"` // 创建时间
	OrderNo     int       `json:"orderNo" xorm:"order_no"`         // 排序
}
