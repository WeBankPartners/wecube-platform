package remote

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"io"
	"net/http"
	"net/url"
	"strings"
)

func GetPluginDataModels(pluginName, token string) (result []*models.SyncDataModelCiType, err error) {
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

type ExpressionObj struct {
	Entity          string    `json:"entity"`
	LeftJoinColumn  string    `json:"leftJoinColumn"`
	RightJoinColumn string    `json:"rightJoinColumn"`
	ResultColumn    string    `json:"resultColumn"`
	RefColumn       string    `json:"refColumn"`
	Filters         []*Filter `json:"filters"`
}

type Filter struct {
	Name     string `json:"name"`
	Operator string `json:"operator"`
	Value    string `json:"value"`
}

func analyzeExpression(express string) (result []*ExpressionObj, err error) {
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
					express += fmt.Sprintf("%s'$%d'", v, i/2)
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
	var expressionSqlList []*ExpressionObj
	for i, ci := range ciList {
		eso := ExpressionObj{}
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
		for ci[0] == 123 {
			if rIdx := strings.Index(ci, "}"); rIdx > 0 {
				tmpFilterList := strings.Split(ci[1:rIdx], " ")
				tmpFilter := Filter{Name: tmpFilterList[0], Operator: tmpFilterList[1], Value: tmpFilterList[2]}
				for fpIndex, fpValue := range filterParams {
					tmpFilter.Value = strings.ReplaceAll(tmpFilter.Value, fmt.Sprintf("$%d", fpIndex), fpValue)
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
		expressionSqlList = append(expressionSqlList, &eso)
	}
	result = expressionSqlList
	return
}
