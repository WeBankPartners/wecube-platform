package models

type ItsdangerousCheckResult struct {
	Code    int                          `json:"code"`    // 小于400是正常
	Status  string                       `json:"status"`  // 正常时返回OK
	Message string                       `json:"message"` // 版本
	Data    *ItsdangerousCheckResultData `json:"data"`    // 数据
}

type ItsdangerousCheckResultData struct {
	Text string                             `json:"text"` // 高危结果描述
	Data []*ItsdangerousCheckResultDataItem `json:"data"` // 正常时返回OK
}

type ItsdangerousCheckResultDataItem struct {
	Lineno     []int  `json:"lineno"`      // [开始行，结束行]
	Level      string `json:"level"`       // 级别
	Content    string `json:"content"`     // 高危内容
	Message    string `json:"message"`     // 高危提示
	ScriptName string `json:"script_name"` // 脚本名称
}
