package monitor

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"io"
	"net/http"
)

const (
	analyzeMonitorExportDataUrl  = "/monitor/api/v2/trans-export/analyze"
	exportMetricListUrl          = "/monitor/api/v2/monitor/metric/export?serviceGroup=%s&monitorType=%s&endpointGroup=%s&comparison=%s"
	queryMonitorEndpointGroupUrl = "/monitor/api/v1/alarm/endpoint_group/query"
	queryMonitorLogMetricUrl     = "/monitor/api/v2/service/log_metric/list/group/%s"
	queryAlarmStrategyUrl        = "/monitor/api/v2/alarm/strategy/query"
	queryLogKeywordUrl           = "/monitor/api/v2/service/log_keyword/list?type=service&guid=%s"
	queryDbKeywordUrl            = "/monitor/api/v2/service/db_keyword/list?type=service&guid=%s"
	jsonUnmarshalErrTemplate     = "json unmarshal http response body fail,body:%s,error:%s"
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

func ExportMetricList(param ExportMetricParam) (responseBytes []byte, err error) {
	url := fmt.Sprintf(exportMetricListUrl, param.ServiceGroup, param.MonitorType, param.EndpointGroup, param.Comparison)
	if responseBytes, err = requestMonitorPlugin(url, http.MethodGet, nil); err != nil {
		return
	}
	return
}

func GetMonitorEndpointGroup() (monitorEndpointGroupList []*EndpointGroupTable, err error) {
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(queryMonitorEndpointGroupUrl, http.MethodGet, nil); err != nil {
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
	monitorEndpointGroupList = response.Data
	return
}

func GetLogMonitorAndTemplate(serviceGroupList []string) (matchServiceGroupList, logMonitorTemplateList []string, err error) {
	for _, serviceGroup := range serviceGroupList {
		containLogMonitor, tmpTemplateList, tmpErr := analyzeLogMonitorIfNeedExport(serviceGroup)
		if tmpErr != nil {
			err = fmt.Errorf("analyze log montior with serviceGroup:%s fail,%s ", serviceGroup, tmpErr.Error())
			break
		}
		if containLogMonitor {
			matchServiceGroupList = append(matchServiceGroupList, serviceGroup)
			logMonitorTemplateList = append(logMonitorTemplateList, tmpTemplateList...)
		}
	}
	return
}

func analyzeLogMonitorIfNeedExport(serviceGroup string) (containLogMonitor bool, logMonitorTemplateList []string, err error) {
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(fmt.Sprintf(queryMonitorLogMetricUrl, serviceGroup), http.MethodGet, nil); err != nil {
		return
	}
	var response GetLogMonitorResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if len(response.Data) > 0 {
		if len(response.Data[0].Config) > 0 || len(response.Data[0].DBConfig) > 0 {
			containLogMonitor = true
		}
		for _, v := range response.Data[0].Config {
			for _, logMetricGroup := range v.MetricGroups {
				if logMetricGroup.LogMonitorTemplate != "" {
					logMonitorTemplateList = append(logMonitorTemplateList, logMetricGroup.LogMonitorTemplate)
				}
			}
		}
	}
	return
}

func GetAlarmStrategyMatchGroupList(serviceGroupList, endpointGroupList []string) (matchServiceGroupList, matchEndpointGroupList []string, err error) {
	for _, serviceGroup := range serviceGroupList {
		containStrategy, tmpErr := analyzeAlarmStrategyIfNeedExport(serviceGroup, "")
		if tmpErr != nil {
			err = fmt.Errorf("analyze alarm strategy with serviceGroup:%s fail,%s ", serviceGroup, tmpErr.Error())
			break
		}
		if containStrategy {
			matchServiceGroupList = append(matchServiceGroupList, serviceGroup)
		}
	}
	if err != nil {
		return
	}
	for _, endpointGroup := range endpointGroupList {
		containStrategy, tmpErr := analyzeAlarmStrategyIfNeedExport("", endpointGroup)
		if tmpErr != nil {
			err = fmt.Errorf("analyze alarm strategy with endpointGroup:%s fail,%s ", endpointGroup, tmpErr.Error())
			break
		}
		if containStrategy {
			matchEndpointGroupList = append(matchEndpointGroupList, endpointGroup)
		}
	}
	return
}

func analyzeAlarmStrategyIfNeedExport(serviceGroup, endpointGroup string) (containStrategy bool, err error) {
	requestParam := AlarmStrategyQueryParam{}
	if serviceGroup != "" {
		requestParam = AlarmStrategyQueryParam{Guid: serviceGroup, QueryType: "service", Show: "all"}
	} else if endpointGroup != "" {
		requestParam = AlarmStrategyQueryParam{Guid: endpointGroup, QueryType: "group", Show: "all"}
	}
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(queryAlarmStrategyUrl, http.MethodPost, requestParam); err != nil {
		return
	}
	var response AlarmStrategyQueryResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if len(response.Data) > 0 {
		if len(response.Data[0].Strategy) > 0 {
			containStrategy = true
		}
	}
	return
}

func GetLogKeywordMatchGroupList(serviceGroupList []string) (matchServiceGroupList []string, err error) {
	for _, serviceGroup := range serviceGroupList {
		logKeywordMatchFlag, tmpLogKeywordErr := analyzeLogKeywordIfNeedExport(serviceGroup)
		if tmpLogKeywordErr != nil {
			err = fmt.Errorf("analyze log keyword with serviceGroup:%s fail,%s ", serviceGroup, tmpLogKeywordErr.Error())
			break
		}
		if logKeywordMatchFlag {
			matchServiceGroupList = append(matchServiceGroupList, serviceGroup)
			continue
		}
		dbKeywordMatchFlag, tmpDbKeywordErr := analyzeDbKeywordIfNeedExport(serviceGroup)
		if tmpDbKeywordErr != nil {
			err = fmt.Errorf("analyze db keyword with serviceGroup:%s fail,%s ", serviceGroup, tmpDbKeywordErr.Error())
			break
		}
		if dbKeywordMatchFlag {
			matchServiceGroupList = append(matchServiceGroupList, serviceGroup)
			continue
		}
	}
	return
}

func analyzeLogKeywordIfNeedExport(serviceGroup string) (containConfigFlag bool, err error) {
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(fmt.Sprintf(queryLogKeywordUrl, serviceGroup), http.MethodGet, nil); err != nil {
		return
	}
	var response LogKeywordQueryResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if len(response.Data) > 0 {
		if len(response.Data[0].Config) > 0 {
			containConfigFlag = true
			return
		}
	}
	return
}

func analyzeDbKeywordIfNeedExport(serviceGroup string) (containConfigFlag bool, err error) {
	var responseBytes []byte
	if responseBytes, err = requestMonitorPlugin(fmt.Sprintf(queryDbKeywordUrl, serviceGroup), http.MethodGet, nil); err != nil {
		return
	}
	var response DbKeywordQueryResp
	if err = json.Unmarshal(responseBytes, &response); err != nil {
		err = fmt.Errorf(jsonUnmarshalErrTemplate, string(responseBytes), err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
		return
	}
	if len(response.Data) > 0 {
		if len(response.Data[0].Config) > 0 {
			containConfigFlag = true
			return
		}
	}
	return
}

func requestMonitorPlugin(url, method string, postData interface{}) (responseBytes []byte, err error) {
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
	req.Header.Set("Authorization", remote.GetToken())
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
