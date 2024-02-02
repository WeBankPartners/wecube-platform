package models

type ItsdangerousBatchCheckResult struct {
	Code    int                               `json:"code"`    // 小于400是正常
	Status  string                            `json:"status"`  // 正常时返回OK
	Message string                            `json:"message"` // 消息
	Data    *ItsdangerousBatchCheckResultData `json:"data"`    // 数据
}

type ItsdangerousBatchCheckResultData struct {
	Text string                    `json:"text"` // 高危结果描述
	Data []*ItsdangerousDetailItem `json:"data"` // 正常时返回OK
}

type ItsdangerousDetailItem struct {
	Lineno     []int  `json:"lineno"`      // [开始行，结束行]
	Level      string `json:"level"`       // 级别
	Content    string `json:"content"`     // 高危内容
	Message    string `json:"message"`     // 高危提示
	ScriptName string `json:"script_name"` // 脚本名称
}

type ItsdangerousWorkflowCheckResult struct {
	ResultCode    string                               `json:"resultCode"`    // 0是正常
	ResultMessage string                               `json:"resultMessage"` // 消息
	Results       *ItsdangerousWorkflowCheckResultData `json:"results"`       // 数据
}

type ItsdangerousWorkflowCheckResultData struct {
	Outputs []*ItsdangerousWorkflowCheckResultDataItem `json:"outputs"`
}

type ItsdangerousWorkflowCheckResultDataItem struct {
	IsDanger bool                      `json:"is_danger"`
	Details  []*ItsdangerousDetailItem `json:"details"`
}
