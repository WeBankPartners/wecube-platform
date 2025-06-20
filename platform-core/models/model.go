package models

import (
	"encoding/json"
	"strings"
)

type DataModel struct {
	PluginPackageDataModel
	Entities []*DataModelEntity `json:"entities"`
}

type DataModelEntity struct {
	PluginPackageEntities
	Attributes            []*PluginPackageAttributes `json:"attributes"`
	ReferenceByEntityList []*DataModelRefEntity      `json:"referenceByEntityList"`
	ReferenceToEntityList []*DataModelRefEntity      `json:"referenceToEntityList"`
	LeafEntityList        *DataModelLeafEntityList   `json:"leafEntityList,omitempty"`
}

type DataModelRefEntity struct {
	PluginPackageEntities
	RelatedAttribute *PluginPackageAttributes `json:"relatedAttribute"`
}

type DataModelLeafEntityList struct {
	Name                  string                 `json:"name"`
	PackageName           string                 `json:"packageName"`
	ReferenceByEntityList []*DataModelLeafEntity `json:"referenceByEntityList"`
	ReferenceToEntityList []*DataModelLeafEntity `json:"referenceToEntityList"`
}

type DataModelLeafEntity struct {
	Name        string `json:"name"`
	EntityName  string `json:"entityName"`
	PackageName string `json:"packageName"`
	FilterRule  string `json:"filterRule"`
}

type EntityQueryParam struct {
	Criteria          EntityQueryObj    `json:"criteria"`
	AdditionalFilters []*EntityQueryObj `json:"additionalFilters"`
}

type EntityQueryObj struct {
	AttrName  string      `json:"attrName"`
	Op        string      `json:"op"`
	Condition interface{} `json:"condition"`
}

type EntityResponse struct {
	Status  string                   `json:"status"`
	Message string                   `json:"message"`
	Data    []map[string]interface{} `json:"data"`
}

type SyncDataModelResponse struct {
	Status  string                 `json:"status"`
	Message string                 `json:"message"`
	Data    []*SyncDataModelCiType `json:"data"`
}

type SyncDataModelCiType struct {
	Name        string                 `json:"name" xorm:"id"`
	DisplayName string                 `json:"displayName" xorm:"display_name"`
	Description string                 `json:"description" xorm:"description"`
	Attributes  []*SyncDataModelCiAttr `json:"attributes" xorm:"-"`
}

type SyncDataModelCiAttr struct {
	Name             string `json:"name" xorm:"name"`
	EntityName       string `json:"entityName" xorm:"ci_type"`
	Description      string `json:"description" xorm:"description"`
	DataType         string `json:"dataType" xorm:"input_type"`
	RefPackageName   string `json:"refPackageName" xorm:"-"`
	RefEntityName    string `json:"refEntityName" xorm:"ref_ci_type"`
	RefAttributeName string `json:"refAttributeName" xorm:"-"`
	Required         string `json:"required" xorm:"nullable"`
	Multiple         string `json:"multiple"`
}

type QueryExpressionDataParam struct {
	DataModelExpression string                       `json:"dataModelExpression"`
	Filters             []*QueryExpressionDataFilter `json:"filters"`
	StartIndex          int                          `json:"startIndex"`
	PageSize            int                          `json:"pageSize"`
	Query               string                       `json:"query"`
	Sorting             []*QueryRequestSorting       `json:"sorting"`
}

type QueryExpressionDataFilter struct {
	Index            int                              `json:"index"`
	PackageName      string                           `json:"packageName"`
	EntityName       string                           `json:"entityName"`
	AttributeFilters []*QueryExpressionDataAttrFilter `json:"attributeFilters"`
}

type QueryExpressionDataAttrFilter struct {
	Name     string      `json:"name"`
	Operator string      `json:"operator"`
	Value    interface{} `json:"value"`
}

type ExpressionObj struct {
	Package         string    `json:"package"`
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

func (f *Filter) GetValue() interface{} {
	if f.Operator == "in" {
		valueList := []string{}
		if strings.HasPrefix(f.Value, "[") && strings.HasSuffix(f.Value, "]") {
			tmpValue := strings.ReplaceAll(f.Value, "'", "\"")
			if err := json.Unmarshal([]byte(tmpValue), &valueList); err == nil {
				newValueList := []string{}
				for _, v := range valueList {
					if strings.HasPrefix(v, "@@") {
						newValueList = append(newValueList, strings.Split(v[2:], "@@")[0])
					}
				}
				valueList = newValueList
			}
		} else {
			for _, v := range strings.Split(f.Value, ",") {
				if v != "" {
					valueList = append(valueList, v)
				}
			}
		}
		return valueList
	} else if f.Operator == "eq" {
		if strings.HasPrefix(f.Value, "@@") {
			f.Value = strings.Split(f.Value[2:], "@@")[0]
		}
	}
	return f.Value
}

type ExpressionEntitiesRespObj struct {
	PackageName string                     `json:"packageName"`
	EntityName  string                     `json:"entityName"`
	Attributes  []*PluginPackageAttributes `json:"attributes"`
}

type PluginQueryExpressionDataParam struct {
	DataModelExpression string `json:"dataModelExpression" binding:"required"`
	RootDataId          string `json:"rootDataId" binding:"required"`
	Token               string `json:"token"`
}

type BuildContextParam struct {
	TransactionId string
	UserId        string
	Roles         []string
	Token         string
	Language      string
}

type RoleEntityResp struct {
	Status  string           `json:"status"`
	Message string           `json:"message"`
	Data    []*RoleEntityObj `json:"data"`
}

type RoleEntityObj struct {
	Id          string `json:"id" xorm:"guid"`
	DisplayName string `json:"displayName" xorm:"display_name"`
	Email       string `json:"email" xorm:"email"`
}
