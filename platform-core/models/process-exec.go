package models

type ProcDefListObj struct {
	ProcDefId      string `json:"procDefId"`
	ProcDefKey     string `json:"procDefKey"`
	ProcDefName    string `json:"procDefName"`
	ProcDefVersion string `json:"procDefVersion"`
	ProcDefData    string `json:"procDefData"`
	RootEntity     string `json:"rootEntity"`
	Status         string `json:"status"`
	Tags           string `json:"tags"`
	ExcludeMode    string `json:"excludeMode"`
	CreatedTime    string `json:"createdTime"`
	Scene          string `json:"scene"`
}
