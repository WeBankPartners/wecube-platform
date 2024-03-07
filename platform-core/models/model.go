package models

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
}

type QueryExpressionDataFilter struct {
	Index            int                              `json:"index"`
	PackageName      string                           `json:"packageName"`
	EntityName       string                           `json:"entityName"`
	AttributeFilters []*QueryExpressionDataAttrFilter `json:"attributeFilters"`
}

type QueryExpressionDataAttrFilter struct {
	Name     string `json:"name"`
	Operator string `json:"operator"`
	Value    string `json:"value"`
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

type ExpressionEntitiesRespObj struct {
	PackageName string                     `json:"packageName"`
	EntityName  string                     `json:"entityName"`
	Attributes  []*PluginPackageAttributes `json:"attributes"`
}
