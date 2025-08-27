package remote

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/url"
	"os"
	"reflect"
	"strings"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/network"
	"go.uber.org/zap"

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
	// Example expression -> "wecmdb:app_instance~(host_resource)wecmdb:host_resource{ip_address eq '127.0.0.1'}{code in '222'}.resource_set>wecmdb:resource_set.code"
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
		if len(lastQueryResult) == 0 {
			break
		}
		if i == exprLastIndex && rootFilter.Index == i && len(lastQueryResult) > 0 {
			// 表达式与根表达式一样
			log.Debug(nil, log.LOGGER_APP, "QueryPluginFullData expr same with root", zap.Int("index", i), zap.Int("rootIndex", rootFilter.Index), log.JsonObj("lastQueryResult", lastQueryResult))
			rootEntityNode.LastFlag = true
			resultNodeList = append(resultNodeList, rootEntityNode)
			continue
		}
		log.Debug(nil, log.LOGGER_APP, "QueryPluginFullData expr", zap.Int("index", i), zap.Int("rootIndex", rootFilter.Index), log.JsonObj("tmpLeftDataMap", tmpLeftDataMap))
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
					log.Debug(nil, log.LOGGER_APP, "QueryPluginFullData handle row,LeftJoinColumn", zap.String("id", rowDataId), zap.String("LeftJoinColumn", exprObj.LeftJoinColumn))
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
					log.Debug(nil, log.LOGGER_APP, "QueryPluginFullData handle row,RightJoinColumn1", zap.String("id", rowDataId), zap.String("RightJoinColumn", exprObj.LeftJoinColumn))
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
						log.Debug(nil, log.LOGGER_APP, "QueryPluginFullData handle row,RightJoinColumn2", zap.Strings("attrIdList", attrIdList), zap.String("tmpMatchRightFullDataId", tmpMatchRightFullDataId))
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
		log.Debug(nil, log.LOGGER_APP, "dataFullIdMap", zap.String("k", k), zap.String("v", v))
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
	if expr.ResultColumn == "" {
		expr.ResultColumn = "id"
	}
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
			log.Info(nil, log.LOGGER_APP, "RequestPluginModelData trans filter value to []interface{} ", zap.String("name", v.AttrName), zap.String("op", v.Op))
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
	log.Info(nil, log.LOGGER_APP, "Start remote modelData request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token), zap.String("requestBody", string(postBytes)))
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
			log.Error(nil, log.LOGGER_APP, "End remote modelData request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote modelData request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(responseBodyBytes)))
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
	log.Info(nil, log.LOGGER_APP, "Start remote modelData create --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), zap.String("operation", operation), log.JsonObj("Authorization", token), zap.String("requestBody", string(postBytes)))
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
			log.Error(nil, log.LOGGER_APP, "End remote modelData create <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote modelData create <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(responseBodyBytes)))
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
	log.Info(nil, log.LOGGER_APP, "Start remote modelData update --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token), zap.String("requestBody", string(postBytes)))
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
			log.Error(nil, log.LOGGER_APP, "End remote modelData update <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote modelData update <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(responseBodyBytes)))
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
	log.Info(nil, log.LOGGER_APP, "Start remote dangerousBatchCheck request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token), zap.String("requestBody", string(reqBody)))
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
			log.Error(nil, log.LOGGER_APP, "End remote dangerousBatchCheck request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote dangerousBatchCheck request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
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
	log.Info(nil, log.LOGGER_APP, "Start remote dangerousWorkflowCheck request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token), zap.String("requestBody", string(reqBody)))
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
			log.Error(nil, log.LOGGER_APP, "End remote dangerousWorkflowCheck request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote dangerousWorkflowCheck request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
		}
	}()
	log.Debug(nil, log.LOGGER_APP, "End remote dangerousWorkflowCheck request 1111", zap.String("respBody", string(respBody)))
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
	log.Info(nil, log.LOGGER_APP, "Start remote pluginInterfaceApi request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token), zap.String("requestBody", string(reqBodyPtr)))
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
			log.Error(nil, log.LOGGER_APP, "End remote pluginInterfaceApi request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote pluginInterfaceApi request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
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

// nexus 推送物料包
func PushPackage(ctx context.Context, token string, unitDesignId string, deployPackageId string, subDirPath string) (result *models.PushArtifactPluginPackageData, err error) {
	uri := fmt.Sprintf("%s/%s/unit-designs/%s/packages/%s/push", models.Config.Gateway.Url, models.PluginNameArtifacts, unitDesignId, deployPackageId)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	postData := models.PushArtifactPluginPackageParam{Path: subDirPath}
	postBytes, _ := json.Marshal(postData)
	urlObj, _ := url.Parse(uri)
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), bytes.NewReader(postBytes))
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
	log.Info(nil, log.LOGGER_APP, "Start remote pushPackage request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()), log.JsonObj("Authorization", token))

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
			log.Error(nil, log.LOGGER_APP, "End remote pushPackage request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote pushPackage request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
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

func UploadArtifactPackageNew(ctx context.Context, token string, unitDesignId string, localPackagePath string) (deployPackageGuid string, err error) {
	uri := fmt.Sprintf("%s/%s/unit-designs/%s/packages/upload", models.Config.Gateway.Url, models.PluginNameArtifacts, unitDesignId)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	buf := new(bytes.Buffer)
	bodyWriter := multipart.NewWriter(buf)
	if fileObj, tmpErr := os.Open(localPackagePath); tmpErr != nil {
		err = fmt.Errorf("can not read multipart form file:%s ,err:%s", localPackagePath, tmpErr.Error())
		return
	} else {
		fileName := localPackagePath
		if lastPathIndex := strings.LastIndex(localPackagePath, "/"); lastPathIndex > 0 {
			fileName = localPackagePath[lastPathIndex+1:]
		}
		tmpWriter, ffErr := bodyWriter.CreateFormFile("file", fileName)
		if ffErr != nil {
			err = fmt.Errorf("create multipart form file fail,key:file,%s ", err.Error())
			return
		}
		if _, err = io.Copy(tmpWriter, fileObj); err != nil {
			err = fmt.Errorf("io copy multipart file fail,%s ", err.Error())
			return
		}
		packageTypeWriter, _ := bodyWriter.CreateFormField("package_type")
		_, writeErr := packageTypeWriter.Write([]byte("APP&DB"))
		if writeErr != nil {
			err = fmt.Errorf("create form field package type value fail,%s ", writeErr.Error())
			return
		}
	}
	bodyWriter.Close()
	req, reqErr := http.NewRequest(http.MethodPost, urlObj.String(), buf)
	if reqErr != nil {
		err = fmt.Errorf("new http request to %s fail,%s ", urlObj.String(), reqErr.Error())
		return
	}
	req.Header.Set("Content-Type", bodyWriter.FormDataContentType())
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

	startTime := time.Now()
	log.Info(nil, log.LOGGER_APP, "Start remote uploadPackage to artifacts plugin request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}

	var response models.PluginArtifactsUploadResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "End remote uploadPackage to artifacts plugin request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote uploadPackage to artifacts plugin request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
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
	if len(response.Data) > 0 {
		if v, ok := response.Data[0]["guid"]; ok {
			deployPackageGuid = fmt.Sprintf("%s", v)
		}
	}
	return
}

func UploadArtifactPackage(ctx context.Context, token string, unitDesignId string, localPackagePath string) (deployPackageGuid string, err error) {
	uri := fmt.Sprintf("%s/%s/unit-designs/%s/packages/upload", models.Config.Gateway.Url, models.PluginNameArtifacts, unitDesignId)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	urlObj, _ := url.Parse(uri)
	var req *http.Request
	r, w := io.Pipe()
	bodyWriter := multipart.NewWriter(w)
	go func() {
		defer w.Close()
		defer bodyWriter.Close()
		if fileObj, tmpErr := os.Open(localPackagePath); tmpErr != nil {
			err = fmt.Errorf("can not read multipart form file:%s ,err:%s", localPackagePath, tmpErr.Error())
			return
		} else {
			fileName := localPackagePath
			if lastPathIndex := strings.LastIndex(localPackagePath, "/"); lastPathIndex > 0 {
				fileName = localPackagePath[lastPathIndex+1:]
			}
			tmpWriter, ffErr := bodyWriter.CreateFormFile("file", fileName)
			if ffErr != nil {
				err = fmt.Errorf("create multipart form file fail,key:file,%s ", err.Error())
				return
			}
			if _, err = io.Copy(tmpWriter, fileObj); err != nil {
				fileObj.Close()
				err = fmt.Errorf("io copy file to multipart form fail,%s ", err.Error())
				return
			}
			fileObj.Close()
		}
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "build multipart form data fail", zap.Error(err))
		}
	}()
	if err != nil {
		return
	}
	req, err = http.NewRequest(http.MethodPost, urlObj.String(), r)
	if err != nil {
		err = fmt.Errorf("new http request to %s fail,%s ", urlObj.String(), err.Error())
		return
	}
	req.Header.Set("Content-Type", bodyWriter.FormDataContentType())
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

	startTime := time.Now()
	log.Info(nil, log.LOGGER_APP, "Start remote uploadPackage to artifacts plugin request --->>> ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("method", http.MethodPost), zap.String("url", urlObj.String()))
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do request fail,%s ", respErr.Error())
		return
	}

	var response models.PluginArtifactsUploadResult
	respBody, readBodyErr := io.ReadAll(resp.Body)
	defer func() {
		if resp.Body != nil {
			resp.Body.Close()
		}
		useTime := fmt.Sprintf("%.3fms", time.Since(startTime).Seconds()*1000)
		if err != nil {
			log.Error(nil, log.LOGGER_APP, "End remote uploadPackage to artifacts plugin request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.Error(err))
		} else {
			log.Info(nil, log.LOGGER_APP, "End remote uploadPackage to artifacts plugin request <<<--- ", zap.String("requestId", reqId), zap.String("transactionId", transId), zap.String("url", urlObj.String()), zap.Int("httpCode", resp.StatusCode), zap.String("costTime", useTime), zap.String("response", string(respBody)))
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
	if len(response.Data) > 0 {
		if v, ok := response.Data[0]["guid"]; ok {
			deployPackageGuid = fmt.Sprintf("%s", v)
		}
	}
	return
}

func QueryBusinessList(query models.QueryBusinessListParam) (result []map[string]interface{}, err error) {
	var responseBodyBytes []byte
	var response models.EntityResponse
	uri := fmt.Sprintf("%s/%s/entities/%s/query", models.Config.Gateway.Url, query.PackageName, query.Entity)
	postBytes, _ := json.Marshal(query.EntityQueryParam)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	if responseBodyBytes, err = network.HttpPost(uri, query.UserToken, query.Language, postBytes); err != nil {
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

func AutoConfirmCMDBView(ctx context.Context, viewId string) (err error) {
	log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView start", zap.String("viewId", viewId))
	structQueryBytes, structQueryErr := network.HttpGet(fmt.Sprintf("http://%s/wecmdb/api/v1/view/%s", models.Config.Gateway.Url, viewId), GetToken(), models.DefaultLanguage)
	if structQueryErr != nil {
		err = fmt.Errorf("query cmdb view struct fail,view:%s ,error:%s ", viewId, structQueryErr.Error())
		return
	}
	log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView query cmdb view struct done", zap.String("viewId", viewId), zap.String("response", string(structQueryBytes)))
	var structQueryResp models.CMDBViewStructQueryResp
	if err = json.Unmarshal(structQueryBytes, &structQueryResp); err != nil {
		err = fmt.Errorf("json unmarshal query cmdb view struct response:%s fail,%s ", string(structQueryBytes), err.Error())
		return
	}
	if structQueryResp.StatusCode != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(structQueryResp.StatusMessage)
		return
	}
	if structQueryResp.Data == nil || structQueryResp.Data.CiType == "" {
		err = fmt.Errorf("query cmdb view struct fail with illegal response:%s ", string(structQueryBytes))
		return
	}
	if structQueryResp.Data.SupportVersion != "yes" {
		log.Warn(nil, log.LOGGER_APP, "AutoConfirmCMDBView ignore with view supportView=no", zap.String("viewId", viewId))
		return
	}
	queryCiDataParam := models.QueryCiDataRequestParam{Dialect: &models.QueryRequestDialect{QueryMode: "new"}, Sorting: &models.QueryRequestSorting{Asc: false, Field: "create_time"}, Filters: []*models.QueryRequestFilterObj{}}
	if structQueryResp.Data.FilterAttr != "" {
		queryCiDataParam.Filters = append(queryCiDataParam.Filters, &models.QueryRequestFilterObj{Name: structQueryResp.Data.FilterAttr, Operator: "eq", Value: structQueryResp.Data.FilterValue})
	}
	ciDataParamBytes, _ := json.Marshal(&queryCiDataParam)
	log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView start query ci data", zap.String("viewId", viewId), zap.String("ciType", structQueryResp.Data.CiType))
	ciDataRespBytes, ciDataRespErr := network.HttpPost(fmt.Sprintf("http://%s/wecmdb/api/v1/ci-data/query/%s", models.Config.Gateway.Url, structQueryResp.Data.CiType), GetToken(), models.DefaultLanguage, ciDataParamBytes)
	if ciDataRespErr != nil {
		err = fmt.Errorf("query cmdb ci data fail,%s ", ciDataRespErr.Error())
		return
	}
	log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView query ci data done", zap.String("viewId", viewId), zap.String("ciDataRespBytes", string(ciDataRespBytes)))
	var ciDataQueryResp models.QueryCiDataResp
	if err = json.Unmarshal(ciDataRespBytes, &ciDataQueryResp); err != nil {
		err = fmt.Errorf("json unmarshal query cmdb ci data response:%s fail,%s ", string(ciDataRespBytes), err.Error())
		return
	}
	if ciDataQueryResp.StatusCode != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(ciDataQueryResp.StatusMessage)
		return
	}
	if ciDataQueryResp.Data == nil {
		err = fmt.Errorf("query cmdb ci data fail with illegal response:%s ", string(ciDataRespBytes))
		return
	}
	for _, dataRow := range ciDataQueryResp.Data.Contents {
		tmpGuid := fmt.Sprintf("%s", dataRow["guid"])
		if tmpGuid == "" {
			continue
		}
		log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView start confirm view", zap.String("viewId", viewId), zap.String("ciDataGuid", tmpGuid))
		if err = confirmCMDBView(ctx, viewId, tmpGuid); err != nil {
			err = fmt.Errorf("try to confirm view:%s ciData:%s fail,%s ", viewId, tmpGuid, err.Error())
			break
		}
		log.Debug(nil, log.LOGGER_APP, "AutoConfirmCMDBView confirm view done", zap.String("viewId", viewId), zap.String("ciDataGuid", tmpGuid))
	}
	return
}

func confirmCMDBView(ctx context.Context, viewId, ciDataGuid string) (err error) {
	postParam := models.ConfirmCMDBViewParam{ViewId: viewId, RootCi: ciDataGuid}
	postBytes, _ := json.Marshal(&postParam)
	respBytes, respErr := network.HttpPost(fmt.Sprintf("http://%s/wecmdb/api/v1/view-confirm", models.Config.Gateway.Url), GetToken(), models.DefaultLanguage, postBytes)
	if respErr != nil {
		err = fmt.Errorf("confirm cmdb view fail,%s ", respErr.Error())
		return
	}
	var response models.ConfirmCMDBViewResp
	if err = json.Unmarshal(respBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal confirm cmdb view response:%s fail,%s ", string(respBytes), err.Error())
		return
	}
	if response.StatusCode != models.DefaultHttpSuccessCode {
		err = fmt.Errorf(response.StatusMessage)
	}
	return
}

func ConfirmCMDBDataList(ctx context.Context, ciTypeEntity string, dataGuidList []string) (err error) {
	if len(dataGuidList) == 0 {
		return
	}
	requestParam := models.PluginCiDataOperationRequest{RequestId: "req_" + guid.CreateGuid(), Inputs: []*models.PluginCiDataOperationRequestObj{}}
	var jsonData []map[string]interface{}
	for _, tmpGuid := range dataGuidList {
		tmpRowMap := make(map[string]interface{})
		tmpRowMap["id"] = tmpGuid
		jsonData = append(jsonData, tmpRowMap)
	}
	jsonDataBytes, _ := json.Marshal(jsonData)
	requestParam.Inputs = append(requestParam.Inputs, &models.PluginCiDataOperationRequestObj{CallbackParameter: fmt.Sprintf("%s_%d", ciTypeEntity, time.Now().Unix()), CiType: ciTypeEntity, Operation: "Confirm", JsonData: string(jsonDataBytes)})

	var responseBodyBytes []byte
	var response models.PluginCiDataOperationResp
	uri := fmt.Sprintf("%s/wecmdb/plugin/ci-data/operation", models.Config.Gateway.Url)
	postBytes, _ := json.Marshal(requestParam)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	if responseBodyBytes, err = network.HttpPost(uri, GetToken(), models.DefaultLanguage, postBytes); err != nil {
		return
	}
	if err = json.Unmarshal(responseBodyBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.ResultCode != "0" {
		err = fmt.Errorf(response.ResultMessage)
		return
	}
	return
}

func QueryCMDBReportData(ctx context.Context, reportId string, rootDataGuidList []string) (err error) {
	if reportId == "" || len(rootDataGuidList) == 0 {
		return
	}
	requestParam := models.PluginViewDataQueryParam{ReportId: reportId, RootCiList: rootDataGuidList, WithoutChildren: false}
	var responseBodyBytes []byte
	var response models.PluginViewDataQueryResponse
	uri := fmt.Sprintf("%s/wecmdb/api/v1/view-data", models.Config.Gateway.Url)
	postBytes, _ := json.Marshal(requestParam)
	if models.Config.HttpsEnable == "true" {
		uri = "https://" + uri
	} else {
		uri = "http://" + uri
	}
	if responseBodyBytes, err = network.HttpPost(uri, GetToken(), models.DefaultLanguage, postBytes); err != nil {
		return
	}
	if err = json.Unmarshal(responseBodyBytes, &response); err != nil {
		err = fmt.Errorf("json unmarshal response body fail,%s ", err.Error())
		return
	}
	if response.StatusCode != "OK" {
		err = fmt.Errorf(response.StatusMessage)
		return
	}
	return
}
