package remote

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"reflect"
	"strings"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func GetPluginDataModels(ctx context.Context, pluginName, token string) (result []*models.SyncDataModelCiType, err error) {
	uri := fmt.Sprintf("%s/%s/data-model", models.Config.Gateway.Url, pluginName)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodGet, urlObj.String(), nil)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var response models.SyncDataModelResponse
	respBody, readBodyErr := io.ReadAll(resp.Body)
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	resp.Body.Close()
	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
		return
	}
	result = response.Data
	return
}

func AnalyzeExpression(express string) (result []*models.ExpressionObj, err error) {
	log.Logger.Info("getExpressResultList", log.String("express", express))
	// Example expression -> "wecmdb:app_instance~(host_resource)wecmdb:host_resource{ip_address eq '***REMOVED***'}{code in '222'}.resource_set>wecmdb:resource_set.code"
	var ciList, filterParams, tmpSplitList []string
	// replace content 'xxx' to '$1' in case of content have '>~.:()[]'
	if strings.Contains(express, "'") {
		tmpSplitList = strings.Split(express, "'")
		express = ""
		for i, v := range tmpSplitList {
			if i%2 == 0 {
				if i == len(tmpSplitList)-1 {
					express += v
				} else {
					express += fmt.Sprintf("%s'$%d$'", v, i/2)
				}
			} else {
				filterParams = append(filterParams, strings.ReplaceAll(v, "'", ""))
			}
		}
	}
	// split with > or ~
	var cursor int
	for i, v := range express {
		if v == 62 || v == 126 {
			ciList = append(ciList, express[cursor:i])
			cursor = i
		}
	}
	ciList = append(ciList, express[cursor:])
	// analyze each ci segment
	var expressionSqlList []*models.ExpressionObj
	ciListLen := len(ciList)
	for i, ci := range ciList {
		eso := models.ExpressionObj{}
		if strings.HasPrefix(ci, ">") {
			eso.LeftJoinColumn = ciList[i-1][strings.LastIndex(ciList[i-1], ".")+1:]
			ci = ci[1:]
		} else if strings.HasPrefix(ci, "~") {
			eso.RightJoinColumn = ci[2:strings.Index(ci, ")")]
			eso.RefColumn = eso.RightJoinColumn
			ci = ci[strings.Index(ci, ")")+1:]
		}
		// ASCII . -> 46 , [ -> 91 , ] -> 93 , : -> 58 , { -> 123 , } -> 125
		for j, v := range ci {
			if v == 46 || v == 123 || v == 91 {
				eso.Entity = ci[:j]
				ci = ci[j:]
				break
			}
		}
		if eso.Entity == "" {
			eso.Entity = ci
		}
		for len(ci) > 0 && ci[0] == 123 {
			if rIdx := strings.Index(ci, "}"); rIdx > 0 {
				tmpFilterList := strings.Split(ci[1:rIdx], " ")
				tmpFilterVal := tmpFilterList[2]
				if strings.HasPrefix(tmpFilterVal, "'") {
					tmpFilterVal = tmpFilterVal[1 : len(tmpFilterVal)-1]
				}
				tmpFilter := models.Filter{Name: tmpFilterList[0], Operator: tmpFilterList[1], Value: tmpFilterVal}
				for fpIndex, fpValue := range filterParams {
					tmpFilter.Value = strings.ReplaceAll(tmpFilter.Value, fmt.Sprintf("$%d$", fpIndex), fpValue)
				}
				eso.Filters = append(eso.Filters, &tmpFilter)
				ci = ci[rIdx+1:]
			} else {
				err = fmt.Errorf("expression illegal")
				break
			}
		}
		if err != nil {
			return
		}
		if len(ci) > 0 && ci[0] == 46 {
			if i == ciListLen-1 {
				eso.ResultColumn = ci[1:]
			}
		}
		entitySplitList := strings.Split(eso.Entity, ":")
		if len(entitySplitList) != 2 {
			err = fmt.Errorf("entity-> %s illegal", eso.Entity)
			return
		}
		eso.Package = entitySplitList[0]
		eso.Entity = entitySplitList[1]
		expressionSqlList = append(expressionSqlList, &eso)
	}
	result = expressionSqlList
	return
}

func AnalyzeExprFilters(input string) (filters []*models.Filter, err error) {
	for len(input) > 0 && input[0] == 123 {
		if rIdx := strings.Index(input, "}"); rIdx > 0 {
			tmpFilterList := strings.Split(input[1:rIdx], " ")
			tmpFilterVal := tmpFilterList[2]
			if strings.HasPrefix(tmpFilterVal, "'") {
				tmpFilterVal = tmpFilterVal[1 : len(tmpFilterVal)-1]
			}
			tmpFilter := models.Filter{Name: tmpFilterList[0], Operator: tmpFilterList[1], Value: tmpFilterVal}
			filters = append(filters, &tmpFilter)
			input = input[rIdx+1:]
		} else {
			err = fmt.Errorf("expression illegal")
			break
		}
	}
	return
}

func QueryPluginData(ctx context.Context, exprList []*models.ExpressionObj, filters []*models.QueryExpressionDataFilter, token string) (result []map[string]interface{}, err error) {
	for i, exprObj := range exprList {
		tmpFilters := []*models.EntityQueryObj{}
		if exprObj.Filters != nil {
			for _, exprFilter := range exprObj.Filters {
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: exprFilter.Name, Op: exprFilter.Operator, Condition: exprFilter.GetValue()})
			}
		}
		for _, filterObj := range filters {
			if filterObj.Index == i && exprObj.Package == filterObj.PackageName && exprObj.Entity == filterObj.EntityName {
				for _, extAttrFilter := range filterObj.AttributeFilters {
					tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: extAttrFilter.Name, Op: extAttrFilter.Operator, Condition: extAttrFilter.Value})
				}
			}
		}
		if i > 0 {
			if exprObj.LeftJoinColumn != "" {
				// 左关联，上一个entity的attr关联到自己的id，增加id filter
				var idFilterList []string
				for _, lastResultObj := range result {
					if matchAttrData, ok := lastResultObj[exprObj.LeftJoinColumn]; ok {
						idFilterList = append(idFilterList, getInterfaceStringList(matchAttrData)...)
					}
				}
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: "id", Op: "in", Condition: idFilterList})
			}
			if exprObj.RightJoinColumn != "" {
				// 右关联，自己的attr关联到上一个entity的id，增加attr filter
				var idFilterList []string
				for _, lastResultObj := range result {
					if matchAttrData, ok := lastResultObj["id"]; ok {
						idFilterList = append(idFilterList, getInterfaceStringList(matchAttrData)...)
					}
				}
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: exprObj.RightJoinColumn, Op: "in", Condition: idFilterList})
			}
		}
		result, err = RequestPluginModelData(ctx, exprObj.Package, exprObj.Entity, token, tmpFilters)
		if err != nil {
			break
		}
	}
	return
}

func QueryPluginFullData(ctx context.Context, exprList []*models.ExpressionObj, rootFilter *models.QueryExpressionDataFilter, rootEntityNode *models.ProcPreviewEntityNode, token string, withEntityData bool) (resultNodeList []*models.ProcPreviewEntityNode, err error) {
	dataFullIdMap := make(map[string]string)
	dataFullIdMap[rootEntityNode.DataId] = rootEntityNode.FullDataId
	var tmpQueryResult []map[string]interface{}
	nodePreviousMap := make(map[string][]string)
	nodeSucceedingMap := make(map[string][]string)
	exprLastIndex := len(exprList) - 1
	resultNodeMap := make(map[string]*models.ProcPreviewEntityNode)
	for i, exprObj := range exprList {
		tmpFilters := []*models.EntityQueryObj{}
		if exprObj.Filters != nil {
			for _, exprFilter := range exprObj.Filters {
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: exprFilter.Name, Op: exprFilter.Operator, Condition: exprFilter.GetValue()})
			}
		}
		if rootFilter.Index == i {
			for _, rootAttrFilter := range rootFilter.AttributeFilters {
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: rootAttrFilter.Name, Op: rootAttrFilter.Operator, Condition: rootAttrFilter.Value})
			}
		} else if rootFilter.Index > i {
			continue
		}
		tmpLeftDataMap := make(map[string]string)
		if i > 0 {
			if exprObj.LeftJoinColumn != "" {
				// 左关联，上一个entity的attr关联到自己的id，增加id filter
				var idFilterList []string
				for _, lastResultObj := range tmpQueryResult {
					if matchAttrData, ok := lastResultObj[exprObj.LeftJoinColumn]; ok {
						rowIdFilterList := getInterfaceStringList(matchAttrData)
						idFilterList = append(idFilterList, rowIdFilterList...)
						lastResultObjId := lastResultObj["id"].(string)
						for _, tmpDataFilterId := range rowIdFilterList {
							tmpLeftDataMap[tmpDataFilterId] = lastResultObjId
						}
					}
				}
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: "id", Op: "in", Condition: idFilterList})
			}
			if exprObj.RightJoinColumn != "" {
				// 右关联，自己的attr关联到上一个entity的id，增加attr filter
				var idFilterList []string
				for _, lastResultObj := range tmpQueryResult {
					if matchAttrData, ok := lastResultObj["id"]; ok {
						rowIdFilterList := getInterfaceStringList(matchAttrData)
						idFilterList = append(idFilterList, rowIdFilterList...)
					}
				}
				tmpFilters = append(tmpFilters, &models.EntityQueryObj{AttrName: exprObj.RightJoinColumn, Op: "in", Condition: idFilterList})
			}
		}
		lastQueryResult, lastErr := RequestPluginModelData(ctx, exprObj.Package, exprObj.Entity, token, tmpFilters)
		if lastErr != nil {
			err = lastErr
			break
		}
		if i == exprLastIndex && rootFilter.Index == i && len(lastQueryResult) > 0 {
			// 表达式与根表达式一样
			log.Logger.Debug("QueryPluginFullData expr same with root", log.Int("index", i), log.Int("rootIndex", rootFilter.Index), log.JsonObj("lastQueryResult", lastQueryResult))
			resultNodeList = append(resultNodeList, rootEntityNode)
			continue
		}
		log.Logger.Debug("QueryPluginFullData expr", log.Int("index", i), log.Int("rootIndex", rootFilter.Index), log.JsonObj("tmpLeftDataMap", tmpLeftDataMap))
		if i > rootFilter.Index && len(lastQueryResult) > 0 {
			for _, rowData := range lastQueryResult {
				rowDataId := rowData["id"].(string)
				rowDataNode := &models.ProcPreviewEntityNode{}
				if existNode, existFlag := resultNodeMap[rowDataId]; !existFlag {
					rowDataNode.Parse(exprObj.Package, exprObj.Entity, rowData)
					if withEntityData {
						rowDataNode.EntityData = rowData
					}
					if i == exprLastIndex {
						rowDataNode.LastFlag = true
					}
					resultNodeList = append(resultNodeList, rowDataNode)
					resultNodeMap[rowDataId] = rowDataNode
				} else {
					if i == exprLastIndex {
						existNode.LastFlag = true
					}
				}
				if exprObj.LeftJoinColumn != "" {
					log.Logger.Debug("QueryPluginFullData handle row,LeftJoinColumn", log.String("id", rowDataId), log.String("LeftJoinColumn", exprObj.LeftJoinColumn))
					if leftMapDataId, ok := tmpLeftDataMap[rowDataId]; ok {
						dataFullIdMap[rowDataId] = dataFullIdMap[leftMapDataId] + "::" + rowDataId
						if existPreList, existPreKey := nodePreviousMap[rowDataId]; existPreKey {
							nodePreviousMap[rowDataId] = append(existPreList, leftMapDataId)
						} else {
							nodePreviousMap[rowDataId] = []string{leftMapDataId}
						}
						if existSucList, existSucKey := nodeSucceedingMap[leftMapDataId]; existSucKey {
							nodeSucceedingMap[leftMapDataId] = append(existSucList, rowDataId)
						}
					} else {
						dataFullIdMap[rowDataId] = rowDataId
					}
				}
				if exprObj.RightJoinColumn != "" {
					log.Logger.Debug("QueryPluginFullData handle row,RightJoinColumn1", log.String("id", rowDataId), log.String("RightJoinColumn", exprObj.LeftJoinColumn))
					if matchAttrData, ok := rowData[exprObj.RightJoinColumn]; ok {
						attrIdList := getInterfaceStringList(matchAttrData)
						tmpMatchRightFullDataId := ""
						tmpMatchAttrId := ""
						for _, tmpAttrId := range attrIdList {
							if matchInAttr, existFlag := dataFullIdMap[tmpAttrId]; existFlag {
								tmpMatchRightFullDataId = matchInAttr
								tmpMatchAttrId = tmpAttrId
								break
							}
						}
						log.Logger.Debug("QueryPluginFullData handle row,RightJoinColumn2", log.StringList("attrIdList", attrIdList), log.String("tmpMatchRightFullDataId", tmpMatchRightFullDataId))
						if tmpMatchRightFullDataId != "" {
							dataFullIdMap[rowDataId] = tmpMatchRightFullDataId + "::" + rowDataId
							if existPreList, existPreKey := nodePreviousMap[rowDataId]; existPreKey {
								nodePreviousMap[rowDataId] = append(existPreList, tmpMatchAttrId)
							} else {
								nodePreviousMap[rowDataId] = []string{tmpMatchAttrId}
							}
							if existSucList, existSucKey := nodeSucceedingMap[tmpMatchAttrId]; existSucKey {
								nodeSucceedingMap[tmpMatchAttrId] = append(existSucList, rowDataId)
							}
						} else {
							dataFullIdMap[rowDataId] = rowDataId
						}
					}
				}
			}
		}
		tmpQueryResult = lastQueryResult
	}
	for k, v := range dataFullIdMap {
		log.Logger.Debug("dataFullIdMap", log.String("k", k), log.String("v", v))
	}
	for _, v := range resultNodeList {
		v.FullDataId = dataFullIdMap[v.DataId]
		if preList, ok := nodePreviousMap[v.DataId]; ok {
			v.PreviousIds = preList
		}
		if sucList, ok := nodeSucceedingMap[v.DataId]; ok {
			v.SucceedingIds = sucList
		}
	}
	return
}

func ExtractExpressionResultColumn(exprList []*models.ExpressionObj, exprResult []map[string]interface{}) (result []interface{}) {
	if len(exprResult) == 0 || len(exprList) == 0 {
		return
	}
	expr := exprList[len(exprList)-1]
	result = make([]interface{}, 0)
	for _, r := range exprResult {
		if v, ok := r[expr.ResultColumn]; ok {
			result = append(result, v)
		}
		//} else {
		//	result = append(result, nil)
		//}
	}
	return
}

func RequestPluginModelData(ctx context.Context, packageName, entity, token string, filters []*models.EntityQueryObj) (result []map[string]interface{}, err error) {
	for _, v := range filters {
		if v.Op == "in" && v.Condition == nil {
			v.Condition = []interface{}{}
			log.Logger.Info("RequestPluginModelData trans filter value to []interface{} ", log.String("name", v.AttrName), log.String("op", v.Op))
		}
	}
	queryParam := models.EntityQueryParam{AdditionalFilters: filters}
	postBytes, _ := json.Marshal(queryParam)
	uri := fmt.Sprintf("%s/%s/entities/%s/query", models.Config.Gateway.Url, packageName, entity)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	startTime := time.Now()
	log.Logger.Info("Start remote modelData request --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token), log.String("requestBody", string(postBytes)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var responseBodyBytes []byte
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote modelData request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote modelData request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(responseBodyBytes)))
		}
	}()
	if resp.StatusCode != http.StatusOK {
		err = fmt.Errorf("request plugin:%s model:%s data fail,statusCode:%d ", packageName, entity, resp.StatusCode)
		return
	}
	var response models.EntityResponse
	responseBodyBytes, err = io.ReadAll(resp.Body)
	if err != nil {
		err = fmt.Errorf("read response body fail,%s ", err.Error())
		return
	}
	if err = json.Unmarshal(responseBodyBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
	} else {
		result = response.Data
	}
	return
}

func CreatePluginModelData(ctx context.Context, packageName, entity, token, operation string, datas []map[string]interface{}) (result []map[string]interface{}, err error) {
	postBytes, _ := json.Marshal(datas)
	uri := fmt.Sprintf("%s/%s/entities/%s/create", models.Config.Gateway.Url, packageName, entity)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	if operation != "" {
		req.Header.Set(models.OperationHeader, operation)
	}
	startTime := time.Now()
	log.Logger.Info("Start remote modelData create --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.String("operation", operation), log.JsonObj("Authorization", token), log.String("requestBody", string(postBytes)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var responseBodyBytes []byte
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote modelData create <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote modelData create <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(responseBodyBytes)))
		}
	}()
	var response models.EntityResponse
	responseBodyBytes, err = io.ReadAll(resp.Body)
	if err != nil {
		err = fmt.Errorf("read response body fail,%s ", err.Error())
		return
	}
	if err = json.Unmarshal(responseBodyBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
	} else {
		result = response.Data
	}
	return
}

func UpdatePluginModelData(ctx context.Context, packageName, entity, token, operation string, datas []map[string]interface{}) (result []map[string]interface{}, err error) {
	postBytes, _ := json.Marshal(datas)
	uri := fmt.Sprintf("%s/%s/entities/%s/update", models.Config.Gateway.Url, packageName, entity)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	if operation != "" {
		req.Header.Set(models.OperationHeader, operation)
	}
	startTime := time.Now()
	log.Logger.Info("Start remote modelData update --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token), log.String("requestBody", string(postBytes)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var responseBodyBytes []byte
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote modelData update <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote modelData update <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(responseBodyBytes)))
		}
	}()
	var response models.EntityResponse
	responseBodyBytes, err = io.ReadAll(resp.Body)
	if err != nil {
		err = fmt.Errorf("read response body fail,%s ", err.Error())
		return
	}
	if err = json.Unmarshal(responseBodyBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
	} else {
		result = response.Data
	}
	return
}

func getInterfaceStringList(input interface{}) (guidList []string) {
	if input == nil {
		return
	}
	refType := reflect.TypeOf(input).String()
	if refType == "[]string" {
		guidList = input.([]string)
	} else if refType == "[]interface {}" {
		for _, v := range input.([]interface{}) {
			tmpV := fmt.Sprintf("%s", v)
			if tmpV != "" {
				guidList = append(guidList, tmpV)
			}
		}
	} else {
		tmpV := fmt.Sprintf("%s", input)
		if tmpV != "" {
			guidList = append(guidList, tmpV)
		}
	}
	return
}

func DangerousBatchCheck(ctx context.Context, token string, reqParam interface{}) (result *models.ItsdangerousBatchCheckResultData, err error) {
	uri := fmt.Sprintf("%s/%s/v1/batch_execution_detection", models.Config.Gateway.Url, models.PluginNameItsdangerous)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	var reqBodyReader io.Reader
	reqBody, _ := json.Marshal(reqParam)
	reqBodyReader = bytes.NewReader(reqBody)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), reqBodyReader)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	startTime := time.Now()
	log.Logger.Info("Start remote dangerousBatchCheck request --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token), log.String("requestBody", string(reqBody)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var response models.ItsdangerousBatchCheckResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote dangerousBatchCheck request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote dangerousBatchCheck request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(respBody)))
		}
	}()
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.Status != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.Message)
		return
	}
	result = response.Data
	return
}

func DangerousWorkflowCheck(ctx context.Context, token string, reqParam interface{}) (result *models.ItsdangerousWorkflowCheckResultData, err error) {
	uri := fmt.Sprintf("%s/%s/v1/detection", models.Config.Gateway.Url, models.PluginNameItsdangerous)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	var reqBodyReader io.Reader
	reqBody, _ := json.Marshal(reqParam)
	reqBodyReader = bytes.NewReader(reqBody)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), reqBodyReader)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	var transId string
	if ctx.Value(models.TransactionIdHeader) != nil {
		transId = ctx.Value(models.TransactionIdHeader).(string)
	} else {
		transId = "trans_" + guid.CreateGuid()
	}
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	startTime := time.Now()
	log.Logger.Info("Start remote dangerousWorkflowCheck request --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token), log.String("requestBody", string(reqBody)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var response models.ItsdangerousWorkflowCheckResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote dangerousWorkflowCheck request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote dangerousWorkflowCheck request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(respBody)))
		}
	}()
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.ResultCode != "0" {
		err = fmt.Errorf(response.ResultMessage)
		return
	}
	result = response.Results
	return
}

func PluginInterfaceApi(ctx context.Context, token string, pluginInterface *models.PluginConfigInterfaces, reqParam *models.BatchExecutionPluginExecParam) (result *models.PluginInterfaceApiResultData, errCode string, err error) {
	uri := fmt.Sprintf("%s%s", models.Config.Gateway.Url, pluginInterface.Path)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	var reqBodyPtr []byte
	var reqBodyReader io.Reader
	if reqParam != nil {
		reqBody, _ := json.Marshal(reqParam)
		reqBodyPtr = reqBody
		reqBodyReader = bytes.NewReader(reqBody)
	}
	httpMethod := pluginInterface.HttpMethod
	if httpMethod == "" {
		httpMethod = "POST"
	}
	req, reqErr := http.NewRequest(httpMethod, urlObj.String(), reqBodyReader)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}
	reqId := "req_" + guid.CreateGuid()
	transId := ctx.Value(models.TransactionIdHeader).(string)
	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")
	startTime := time.Now()
	log.Logger.Info("Start remote pluginInterfaceApi request --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token), log.String("requestBody", string(reqBodyPtr)))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}
	var response models.PluginInterfaceApiResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote pluginInterfaceApi request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(respBody)), log.Error(err))
		} else {
			log.Logger.Info("End remote pluginInterfaceApi request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(respBody)))
		}
	}()
	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}
	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	result = response.Results
	errCode = response.ResultCode
	if response.ResultCode != "0" {
		err = fmt.Errorf(response.ResultMessage)
		return
	}
	return
}

func CreateEntityData(ctx context.Context, authToken, packageName, entityName string, data map[string]interface{}) (map[string]interface{}, error) {
	results, err := CreatePluginModelData(ctx, packageName, entityName, authToken, "", []map[string]interface{}{data})
	if err != nil {
		return nil, err
	}
	if len(results) == 0 {
		return nil, fmt.Errorf("empty result data")
	}
	return results[0], nil
}

func UpdateEntityData(ctx context.Context, authToken, packageName, entityName string, data map[string]interface{}) (map[string]interface{}, error) {
	results, err := UpdatePluginModelData(ctx, packageName, entityName, authToken, "", []map[string]interface{}{data})
	if err != nil {
		return nil, err
	}
	if len(results) == 0 {
		return nil, fmt.Errorf("empty result data")
	}
	return results[0], nil
}

func UpdatentityDataWithExpr(ctx context.Context, authToken, packageName, entityName string, rootId string, exprs []*models.ExpressionObj, data map[string]interface{}) error {
	execExprFilterList := make([]*models.QueryExpressionDataFilter, 0)
	execExprFilter := &models.QueryExpressionDataFilter{
		PackageName:      exprs[0].Package,
		EntityName:       exprs[0].Entity,
		AttributeFilters: make([]*models.QueryExpressionDataAttrFilter, 0),
	}
	execExprFilter.AttributeFilters = append(execExprFilter.AttributeFilters, &models.QueryExpressionDataAttrFilter{
		Name:     "id",
		Operator: "eq",
		Value:    rootId,
	})
	execExprFilterList = append(execExprFilterList, execExprFilter)
	leafDatas, err := QueryPluginData(ctx, exprs, execExprFilterList, authToken)
	if err != nil {
		return err
	}
	for _, leafData := range leafDatas {
		newData := make(map[string]interface{})
		for k, v := range data {
			newData[k] = v
		}
		newData["id"] = leafData["id"]
		_, err := UpdatePluginModelData(ctx, exprs[len(exprs)-1].Package, exprs[len(exprs)-1].Entity, authToken, "", []map[string]interface{}{newData})
		if err != nil {
			return err
		}
	}
	return nil
}

func PushPackage(ctx context.Context, token string, unitDesignId string, deployPackageId string) (result map[string]interface{}, err error) {
	uri := fmt.Sprintf("%s/%s/unit-designs/%s/packages/%s/push", models.Config.Gateway.Url, models.PluginNameArtifacts, unitDesignId, deployPackageId)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}

	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), nil)
	if reqErr != nil {
		err = fmt.Errorf("new request fail,%s ", reqErr.Error())
		return
	}

	reqId := "req_" + guid.CreateGuid()
	var transId string
	if ctx.Value(models.TransactionIdHeader) != nil {
		transId = ctx.Value(models.TransactionIdHeader).(string)
	} else {
		transId = "trans_" + guid.CreateGuid()
	}

	req.Header.Set(models.RequestIdHeader, reqId)
	req.Header.Set(models.TransactionIdHeader, transId)
	req.Header.Set(models.AuthorizationHeader, token)
	req.Header.Set("Content-type", "application/json")

	startTime := time.Now()
	log.Logger.Info("Start remote pushPackage request --->>> ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("method", http.MethodPost), log.String("url", urlObj.String()), log.JsonObj("Authorization", token))

	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}

	var response models.PluginArtifactsPushResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Logger.Error("End remote pushPackage request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.Error(err))
		} else {
			log.Logger.Info("End remote pushPackage request <<<--- ", log.String("requestId", reqId), log.String("transactionId", transId), log.String("url", urlObj.String()), log.Int("httpCode", resp.StatusCode), log.String("costTime", useTime), log.String("response", string(respBody)))
		}
	}()

	if readBodyErr != nil {
		err = fmt.Errorf("read response body fail,%s ", readBodyErr.Error())
		return
	}

	if err = json.Unmarshal(respBody, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}

	if response.Code >= 400 {
		err = fmt.Errorf(response.Message)
		return
	}
	result = response.Data
	return
}
