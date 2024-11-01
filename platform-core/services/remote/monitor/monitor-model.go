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
	FilePath      string
}

type CustomDashboardDto struct {
	Name           string            `json:"name"`
	PanelGroupList []string          `json:"panelGroupList"`
	Charts         []*CustomChartDto `json:"charts"`
	MgmtRoles      []string          `json:"mgmtRoles"`
	UseRoles       []string          `json:"useRoles"`
	TimeRange      int               `json:"timeRange"`   //时间范围
	RefreshWeek    int               `json:"refreshWeek"` // 刷新周期
	LogMetricGroup string            `json:"logMetricGroup"`
}

type CustomChartDto struct {
	Id                 string      `json:"id"`
	Public             bool        `json:"public"`
	SourceDashboard    int         `json:"sourceDashboard"`         // 源看板
	Name               string      `json:"name"`                    // 图表名称
	ChartTemplate      string      `json:"chartTemplate"`           // 图表模板
	Unit               string      `json:"unit"`                    // 单位
	ChartType          string      `json:"chartType"`               // 曲线图/饼图,line/pie
	LineType           string      `json:"lineType"`                // 折线/柱状/面积,line/bar/area
	PieType            string      `json:"pieType" xorm:"pie_type"` // 饼图类型
	Aggregate          string      `json:"aggregate"`               // 聚合类型
	AggStep            int         `json:"aggStep"`                 // 聚合间隔
	ChartSeries        interface{} `json:"chartSeries"`
	DisplayConfig      interface{} `json:"displayConfig"`      // 默认所有下面图表位置
	GroupDisplayConfig interface{} `json:"groupDisplayConfig"` // 组下面的图表位置
	Group              string      `json:"group"`              // 所属分组
	LogMetricGroup     *string     `json:"logMetricGroup"`
}

type LogMonitorTemplateIds struct {
	GuidList []string `json:"guidList"`
}

type CommonBatchIdsParam struct {
	Ids []string `json:"ids"`
}

type CustomDashboardExportParam struct {
	Id       int      `json:"id"`
	ChartIds []string `json:"chartIds"`
}

type QueryCustomDashboardResp struct {
	models.HttpResponseMeta
	Data *CustomDashboardDto `json:"data"`
}

type QueryLogMonitorTemplateResp struct {
	models.HttpResponseMeta
	Data []*LogMonitorTemplate `json:"data"`
}

type CommonBatchNameResp struct {
	models.HttpResponseMeta
	Data []string `json:"data"`
}

type BatchAddTypeConfigParam struct {
	DisplayNameList []string `json:"displayNameList"`
}

type BatchAddTypeConfigResp struct {
	models.HttpResponseMeta
	Data interface{} `json:"data"`
}

type ImportMetricParam struct {
	FilePath      string
	UserToken     string
	Language      string
	ServiceGroup  string
	MonitorType   string
	EndpointGroup string
	Comparison    string
}

type ImportMetricResp struct {
	models.HttpResponseMeta
	Data *MetricImportResultDto `json:"data"`
}

type MetricImportResultDto struct {
	SuccessList []string `json:"success_list"` // 成功
	FailList    []string `json:"fail_list"`    // 失败
	Message     string   `json:"message"`      // 描述
}

type ImportStrategyParam struct {
	StrategyType string
	Value        string
	FilePath     string
	UserToken    string
	Language     string
}

type WorkflowDto struct {
	Name    string `json:"name"`
	Version string `json:"version"`
	Key     string `json:"key"`
}

type GetWorkflowResp struct {
	models.HttpResponseMeta
	Data []*WorkflowDto `json:"data"`
}

type LogMonitorTemplate struct {
	Guid             string `json:"guid"`
	Name             string `json:"name"`
	LogType          string `json:"log_type"`
	DemoLog          string `json:"demo_log"`
	JsonRegular      string `json:"json_regular"`
	CreateUser       string `json:"create_user"`
	UpdateUser       string `json:"update_user"`
	UpdateTimeString string `json:"update_time"`
}
