package monitor

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
)

const (
	analyzeMonitorExportDataUrl        = "/monitor/api/v2/trans-export/analyze"
	exportMetricListUrl                = "/monitor/api/v2/monitor/metric/export?serviceGroup=%s&monitorType=%s&endpointGroup=%s&comparison=%s"
	queryMonitorEndpointGroupUrl       = "/monitor/api/v2/alarm/endpoint_group/query"
	exportMonitorLogMetricUrl          = "/monitor/api/v2/service/log_metric/export?serviceGroup=%s"
	exportLogMonitorTemplateUrl        = "/monitor/api/v2/service/log_metric/log_monitor_template/export"
	exportAlarmStrategyUrl             = "/monitor/api/v2/alarm/strategy/export/%s/%s"
	exportKeywordUrl                   = "/monitor/api/v2/service/log_keyword/export?serviceGroup=%s"
	exportCustomDashboardUrl           = "/monitor/api/v2/dashboard/custom/export"
	QueryCustomDashboardUrl            = "/monitor/api/v2/dashboard/custom?id=%d"
	QueryCustomChartPermissionBatchUrl = "/monitor/api/v2/dashboard/custom/batch"
	jsonUnmarshalErrTemplate           = "json unmarshal http response body fail,body:%s,error:%s"
)

func GetMonitorExportAnalyzeData(endpointList, serviceGroupList []string) (data *models.AnalyzeTransData, err error) {
	requestParam := models.AnalyzeTransParam{EndpointList: endpointList, ServiceGroupList: serviceGroupList}
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(analyzeMonitorExportDataUrl, http.MethodPost, requestParam); err != nil {
		return
	}
	var response models.AnalyzeTransResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	data = response.Data
	return
}

func ExportMetricList(param ExportMetricParam, token string) (responseBytes []byte, err error) {
	url := fmt.Sprintf(exportMetricListUrl, param.ServiceGroup, param.MonitorType, param.EndpointGroup, param.Comparison)
	responseBytes, err = requestMonitorPluginV2(url, http.MethodGet, token, nil)
	return
}

func ExportLogMetric(serviceGroup, token string) (responseBytes []byte, err error) {
	url := fmt.Sprintf(exportMonitorLogMetricUrl, serviceGroup)
	responseBytes, err = requestMonitorPluginV2(url, http.MethodGet, token, nil)
	return
}

func ExportLogMonitorTemplate(ids []string, token string) (responseBytes []byte, err error) {
	guids := LogMonitorTemplateIds{GuidList: ids}
	responseBytes, err = requestMonitorPluginV2(exportLogMonitorTemplateUrl, http.MethodPost, token, guids)
	return
}

func ExportAlarmStrategy(queryType, key, token string) (responseBytes []byte, err error) {
	url := fmt.Sprintf(exportAlarmStrategyUrl, queryType, key)
	responseBytes, err = requestMonitorPluginV2(url, http.MethodGet, token, nil)
	return
}

func ExportKeyword(serviceGroup, token string) (responseBytes []byte, err error) {
	url := fmt.Sprintf(exportKeywordUrl, serviceGroup)
	responseBytes, err = requestMonitorPluginV2(url, http.MethodGet, token, nil)
	return
}

func ExportCustomDashboard(id int, chartIds []string, token string) (responseBytes []byte, err error) {
	param := CustomDashboardExportParam{
		Id:       id,
		ChartIds: chartIds,
	}
	responseBytes, err = requestMonitorPluginV2(exportCustomDashboardUrl, http.MethodGet, token, param)
	return
}

func QueryCustomDashboard(id int, token string) (dashboard *CustomDashboardDto, err error) {
	var responseBytes []byte
	var response QueryCustomDashboardResp
	url := fmt.Sprintf(QueryCustomDashboardUrl, id)
	if responseBytes, err = requestMonitorPluginV2(url, http.MethodGet, token, nil); err != nil {
		return
	}
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if response.Data != nil {
		dashboard = response.Data
	}
	return
}

func QueryCustomChartPermissionBatch(ids []string, token string) (roles []string, err error) {
	var response ChartPermissionBatchResp
	var responseBytes []byte
	param := ChartPermissionBatchParam{Ids: ids}
	responseBytes, err = requestMonitorPluginV2(QueryCustomChartPermissionBatchUrl, http.MethodPost, token, param)
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if response.Data != nil {
		roles = response.Data
	}
	return
}

func GetMonitorEndpointGroup(token string) (monitorEndpointGroupList []*EndpointGroupTable, err error) {
	var responseBytes []byte
	if responseBytes, err = requestMonitorPluginV2(queryMonitorEndpointGroupUrl, http.MethodGet, token, nil); err != nil {
		return
	}
	var response GetMonitorEndpointGroupResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if response.Data != nil {
		monitorEndpointGroupList = response.Data.Data
	}
	return
}

func requestMonitorPlugin(url, method string, postData interface{}) ([]byte, error) {
	token := remote.GetToken()
	return requestMonitorPluginV2(url, method, token, postData)
}

func requestMonitorPluginV2(url, method, token string, postData interface{}) (responseBytes []byte, err error) {
	var req *http.Request
	if method == http.MethodGet {
		req, err = http.NewRequest(http.MethodGet, "http://"+models.Config.Gateway.Url+url, nil)
	} else if method == http.MethodPost {
		postBytes, jsonParseErr := json.Marshal(postData)
		if jsonParseErr != nil {
			err = fmt.Errorf("json marshal postData fail,%s ", jsonParseErr.Error())
			return
		}
		req, err = http.NewRequest(http.MethodPost, "http://"+models.Config.Gateway.Url+url, bytes.NewReader(postBytes))
	}
	if err != nil {
		err = fmt.Errorf("Start new request to platform fail:%s ", err.Error())
		return
	}
	if token == "" {
		token = remote.GetToken()
	}
	req.Header.Set("Authorization", token)
	req.Header.Set("Content-Type", "application/json")
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("Start do request to platform fail:%s ", respErr.Error())
		return
	}
	if responseBytes, err = io.ReadAll(resp.Body); err != nil {
		err = fmt.Errorf("Try to read response body fail,%s ", err.Error())
		return
	}
	resp.Body.Close()
	return
}
