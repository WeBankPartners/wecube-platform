package process

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/remote"
	"github.com/gin-gonic/gin"
)

func ProcDefList(c *gin.Context) {
	includeDraft := c.Query("includeDraft") // 0 | 1
	permission := c.Query("permission")     // USE | MGMT
	tag := c.Query("tag")
	if includeDraft == "" {
		includeDraft = "0"
	}
	if permission == "" {
		permission = "USE"
	}
	log.Logger.Debug("procDefList", log.String("includeDraft", includeDraft), log.String(permission, "permission"), log.String("tag", tag))
	result, err := database.ProcDefList(c, includeDraft, permission, tag, middleware.GetRequestRoles(c))
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcDefOutline(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	log.Logger.Debug("ProcDefOutline", log.String("procDefId", procDefId))
	result, err := database.ProcDefOutline(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcDefRootEntities(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	log.Logger.Debug("ProcDefRootEntities", log.String("procDefId", procDefId))
	procDefObj, err := database.GetSimpleProcDefRow(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	exprList, analyzeErr := remote.AnalyzeExpression(procDefObj.RootEntity)
	if analyzeErr != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, analyzeErr))
		return
	}
	result, queryErr := remote.QueryPluginData(c, exprList, []*models.QueryExpressionDataFilter{}, c.GetHeader(models.AuthorizationHeader))
	if queryErr != nil {
		middleware.ReturnError(c, queryErr)
	} else {
		middleware.ReturnData(c, result)
	}
}

func ProcDefPreview(c *gin.Context) {
	procDefId := c.Param("proc-def-id")
	entityDataId := c.Param("entityDataId")
	log.Logger.Debug("ProcDefPreview", log.String("procDefId", procDefId), log.String("entityDataId", entityDataId))
	procOutlineData, err := database.ProcDefOutline(c, procDefId)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	rootExprList, analyzeErr := remote.AnalyzeExpression(procOutlineData.RootEntity)
	if analyzeErr != nil {
		middleware.ReturnError(c, analyzeErr)
		return
	}
	rootLastExprObj := rootExprList[len(rootExprList)-1]
	rootFilter := models.QueryExpressionDataFilter{
		Index:       len(rootExprList) - 1,
		PackageName: rootLastExprObj.Package,
		EntityName:  rootLastExprObj.Entity,
		AttributeFilters: []*models.QueryExpressionDataAttrFilter{{
			Name:     "id",
			Operator: "eq",
			Value:    entityDataId,
		}},
	}
	rootData, getRootDataErr := remote.QueryPluginData(c, rootExprList, []*models.QueryExpressionDataFilter{&rootFilter}, remote.GetToken())
	if getRootDataErr != nil {
		middleware.ReturnError(c, getRootDataErr)
		return
	}
	log.Logger.Debug("rootData", log.String("entityDataId", entityDataId), log.JsonObj("data", rootData))
	for _, node := range procOutlineData.FlowNodes {
		if node.OrderedNo != "" && node.RoutineExpression != "" {
			tmpQueryDataParam := models.QueryExpressionDataParam{DataModelExpression: node.RoutineExpression, Filters: []*models.QueryExpressionDataFilter{&rootFilter}}
			nodeDataList, nodeDataErr := queryProcPreviewNodeData(c, &tmpQueryDataParam)
			if nodeDataErr != nil {
				err = nodeDataErr
				break
			}
			log.Logger.Debug("nodeData", log.String("node", node.NodeId), log.JsonObj("data", nodeDataList))

		}
	}
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
}

func queryProcPreviewNodeData(ctx context.Context, param *models.QueryExpressionDataParam) (dataList []map[string]interface{}, err error) {
	exprList, analyzeErr := remote.AnalyzeExpression(param.DataModelExpression)
	if analyzeErr != nil {
		err = analyzeErr
		return
	}
	dataList, err = remote.QueryPluginData(ctx, exprList, param.Filters, remote.GetToken())
	return
}

func ProcInsTaskNodeBindings(c *gin.Context) {
	sessionId := c.Param("sessionId")
	log.Logger.Debug("ProcInsTaskNodeBindings", log.String("sessionId", sessionId))
}

func ProcInsStart(c *gin.Context) {

}

func ProcInsList(c *gin.Context) {

}

func ProcInsDetail(c *gin.Context) {
	procInsId := c.Param("procInsId")
	log.Logger.Debug("ProcInsDetail", log.String("procInsId", procInsId))
}
