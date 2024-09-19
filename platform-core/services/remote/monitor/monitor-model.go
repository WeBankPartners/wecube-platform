package monitor

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"time"
)

type GetMonitorEndpointTypeResp struct {
	models.HttpResponseMeta
	Data []string `json:"data"`
}

type GetMonitorEndpointGroupParam struct {
	Search        string   `json:"search"`
	EndpointGroup []string `json:"endpointGroup"` // 对象组
	BasicType     []string `json:"basicType"`     // 基础类型
	Page          int      `json:"page"`
	Size          int      `json:"size"`
	Grp           int      `json:"grp"`
}

type GetMonitorEndpointGroupResp struct {
	models.HttpResponseMeta
	Data *EndpointGroupTableData `json:"data"`
}

type EndpointGroupTableData struct {
	Data []*EndpointGroupTable `json:"data"`
}

type GetMonitorEndpointGroupData struct {
	Data []*AlarmEndpointObj `json:"data"`
	Page int                 `json:"page"`
	Size int                 `json:"size"`
	Num  int                 `json:"num"`
}

type AlarmEndpointObj struct {
	Id         string      `json:"id"`
	Guid       string      `json:"guid"`
	Type       string      `json:"type"`
	GroupsIds  string      `json:"groups_ids"`
	Tags       string      `json:"tags"`
	CreateUser string      `json:"create_user"`
	UpdateUser string      `json:"update_user"`
	UpdateTime string      `json:"update_time"`
	Groups     []*GrpTable `json:"groups"`
}

type GrpTable struct {
	Id           int       `json:"id"`
	Name         string    `json:"name"`
	Parent       int       `json:"parent"`
	Description  string    `json:"description"`
	CreateUser   string    `json:"create_user"`
	UpdateUser   string    `json:"update_user"`
	EndpointType string    `json:"endpoint_type"`
	CreateAt     time.Time `json:"create_at"`
	UpdateAt     time.Time `json:"update_at"`
}

type GetLogMonitorResp struct {
	models.HttpResponseMeta
	Data []*LogMetricQueryObj `json:"data"`
}

type LogMetricQueryObj struct {
	Guid        string                 `json:"guid" xorm:"guid"`
	DisplayName string                 `json:"display_name" xorm:"display_name"`
	Description string                 `json:"description" xorm:"description"`
	Config      []*LogMetricMonitorObj `json:"config"`
	DBConfig    []*DbMetricMonitorObj  `json:"db_config"`
}

type LogMetricMonitorObj struct {
	Guid         string               `json:"guid" xorm:"guid"`
	ServiceGroup string               `json:"service_group" xorm:"service_group"`
	LogPath      string               `json:"log_path" xorm:"log_path"`
	MetricType   string               `json:"metric_type" xorm:"metric_type"`
	MonitorType  string               `json:"monitor_type" xorm:"monitor_type"`
	MetricGroups []*LogMetricGroupObj `json:"metric_groups"`
}

type LogMetricGroupObj struct {
	Guid               string `json:"guid" xorm:"guid"`
	Name               string `json:"name" xorm:"name"`
	LogType            string `json:"log_type" xorm:"log_type"`
	LogMetricMonitor   string `json:"log_metric_monitor" xorm:"log_metric_monitor"`
	LogMonitorTemplate string `json:"log_monitor_template" xorm:"log_monitor_template"`
}

type DbMetricMonitorObj struct {
	Guid         string `json:"guid"`
	ServiceGroup string `json:"service_group"`
}

type AlarmStrategyQueryParam struct {
	QueryType string `json:"queryType"`
	Guid      string `json:"guid"`
	Show      string `json:"show"`
	AlarmName string `json:"alarmName"`
}

type AlarmStrategyQueryResp struct {
	models.HttpResponseMeta
	Data []*EndpointStrategyObj `json:"data"`
}

type EndpointStrategyObj struct {
	EndpointGroup string              `json:"endpoint_group"`
	DisplayName   string              `json:"display_name"`
	MonitorType   string              `json:"monitor_type"`
	ServiceGroup  string              `json:"service_group"`
	Strategy      []*GroupStrategyObj `json:"strategy"`
}

type GroupStrategyObj struct {
	Guid          string `json:"guid"`
	Name          string `json:"name"`
	EndpointGroup string `json:"endpoint_group"`
	Metric        string `json:"metric"`
}

type LogKeywordQueryResp struct {
	models.HttpResponseMeta
	Data []*LogKeywordServiceGroupObj `json:"data"`
}

type LogKeywordServiceGroupObj struct {
	Guid        string                  `json:"guid" xorm:"guid"`
	DisplayName string                  `json:"display_name" xorm:"display_name"`
	Description string                  `json:"description" xorm:"description"`
	Config      []*LogKeywordMonitorObj `json:"config"`
}

type LogKeywordMonitorObj struct {
	Guid         string `json:"guid"`
	ServiceGroup string `json:"service_group"`
	LogPath      string `json:"log_path"`
	MonitorType  string `json:"monitor_type"`
}

type DbKeywordQueryResp struct {
	models.HttpResponseMeta
	Data []*ListDbKeywordData `json:"data"`
}

type ListDbKeywordData struct {
	Guid        string                `json:"guid"`
	DisplayName string                `json:"display_name"`
	Description string                `json:"description"`
	ServiceType string                `json:"service_type"`
	UpdateTime  string                `json:"update_time"`
	UpdateUser  string                `json:"update_user"`
	Config      []*DbKeywordConfigObj `json:"config"`
}

type DbKeywordConfigObj struct {
	Guid         string `json:"guid" xorm:"guid"`                   // 唯一标识
	ServiceGroup string `json:"service_group" xorm:"service_group"` // 业务监控组
	Name         string `json:"name" xorm:"name"`                   // 名称
}

type EndpointGroupTable struct {
	Guid         string `json:"guid" xorm:"guid"`
	DisplayName  string `json:"display_name" xorm:"display_name"`
	Description  string `json:"description" xorm:"description"`
	MonitorType  string `json:"monitor_type" xorm:"monitor_type"`
	ServiceGroup string `json:"service_group" xorm:"service_group"`
	AlarmWindow  string `json:"alarm_window" xorm:"alarm_window"`
	UpdateTime   string `json:"update_time" xorm:"update_time"`
	CreateUser   string `json:"create_user" xorm:"create_user"`
	UpdateUser   string `json:"update_user" xorm:"update_user"`
}

type ExportMetricParam struct {
	ServiceGroup  string
	MonitorType   string
	EndpointGroup string
	Comparison    string
	Token         string
}

type LogMonitorTemplateIds struct {
	GuidList []string `json:"guidList"`
}
