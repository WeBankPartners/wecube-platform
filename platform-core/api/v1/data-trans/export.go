package data_trans

import (
	"fmt"
	"strings"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// QueryBusinessList 查询产品、环境列表
func QueryBusinessList(c *gin.Context) {
	var param models.QueryBusinessParam
	var result []map[string]interface{}
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if result, err = database.QueryBusinessList(c, c.GetHeader("Authorization"), c.GetHeader(middleware.AcceptLanguageHeader), param); err != nil {
		log.Error(c, log.LOGGER_APP, "QueryBusinessList", err.Error())
		// middleware.ReturnError(c, err)
		middleware.ReturnData(c, []map[string]interface{}{})
		return
	}
	middleware.ReturnData(c, result)
}

// CreateExport 创建导出,返回导出Id
func CreateExport(c *gin.Context) {
	var param models.CreateExportParam
	var transExportId string
	var exportCustomer *models.DataTransExportCustomerTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.PIds) == 0 || strings.TrimSpace(param.Env) == "" || len(param.PNames) == 0 || strings.TrimSpace(param.CustomerId) == "" {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	if exportCustomer, err = database.GetTransExportCustomer(c, param.CustomerId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if exportCustomer == nil {
		middleware.ReturnError(c, exterror.New().RequestParamValidateError)
		return
	}
	param.CustomerName = exportCustomer.Name
	if transExportId, err = database.CreateExport(c, param, middleware.GetRequestUser(c)); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, transExportId)
}

// UpdateExport 更新导出
func UpdateExport(c *gin.Context) {
	var param models.UpdateExportParam
	var transExport models.TransExportTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if len(param.TransExportId) == 0 || len(param.PIds) == 0 || strings.TrimSpace(param.Env) == "" || len(param.PNames) == 0 {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if transExport, err = database.GetSimpleTranExport(c, param.TransExportId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if transExport.Status != string(models.TransExportStatusStart) {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("this transExport has run,not allow edit")))
		return
	}
	if err = database.UpdateExport(c, param, middleware.GetRequestUser(c)); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

// ExecExport 执行底座导出
func ExecExport(c *gin.Context) {
	var param models.DataTransExportParam
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if strings.TrimSpace(param.TransExportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("TransExportId is empty")))
		return
	}
	userToken := c.GetHeader("Authorization")
	language := c.GetHeader(middleware.AcceptLanguageHeader)
	// 1. 追加导出信息
	if param, err = database.AutoAppendExportParam(c, userToken, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 2.开始导出,采用异步导出方式
	callParam := &models.CallTransExportActionParam{
		DataTransExportParam: param,
		UserToken:            userToken,
		Language:             language,
	}
	go database.ExecExportAction(c, callParam)
	middleware.ReturnSuccess(c)
}

func ExportDetail(c *gin.Context) {
	transExportId := c.Query("transExportId")
	var detail *models.TransExportDetail
	var err error
	if strings.TrimSpace(transExportId) == "" {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, fmt.Errorf("transExportId is empty")))
		return
	}
	if detail, err = database.GetTransExportDetail(c, transExportId); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, detail)
}

func GetExportListOptions(c *gin.Context) {
	var TransExportHistoryOptions models.TransExportHistoryOptions
	var err error
	if TransExportHistoryOptions, err = database.GetAllTransExportOptions(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, TransExportHistoryOptions)
}

func ExportList(c *gin.Context) {
	var param models.TransExportHistoryParam
	var pageInfo models.PageInfo
	var list []*models.TransExportTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if param.PageSize == 0 {
		param.PageSize = 10
	}
	if pageInfo, list, err = database.QueryTransExportByCondition(c, param); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnPageData(c, pageInfo, list)
}

func CreateOrUpdateExportCustomer(c *gin.Context) {
	var param models.DataTransExportCustomerParam
	var customers []*models.DataTransExportCustomerTable
	var err error
	if err = c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	if strings.TrimSpace(param.Name) == "" || strings.TrimSpace(param.NexusAddr) == "" || strings.TrimSpace(param.NexusAccount) == "" || strings.TrimSpace(param.NexusPwd) == "" {
		middleware.ReturnError(c, fmt.Errorf("param is valid"))
		return
	}
	if customers, err = database.QueryTransExportCustomerByName(c, param.Name); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if param.Id != "" {
		// 编辑
		for _, customer := range customers {
			if customer.Id != param.Id {
				middleware.ReturnError(c, exterror.New().ExportCustomerAddNameExistError)
				return
			}
		}
		exportCustomer := &models.DataTransExportCustomerTable{
			Id:           param.Id,
			Name:         param.Name,
			NexusAddr:    param.NexusAddr,
			NexusAccount: param.NexusAccount,
			NexusPwd:     param.NexusPwd,
			NexusRepo:    param.NexusRepo,
		}
		if err = database.UpdateTransExportCustomer(c, exportCustomer); err != nil {
			middleware.ReturnError(c, err)
			return
		}
	} else {
		// 新增
		if len(customers) > 0 {
			middleware.ReturnError(c, exterror.New().ExportCustomerAddNameExistError)
			return
		}
		exportCustomer := &models.DataTransExportCustomerTable{
			Id:           guid.CreateGuid(),
			Name:         param.Name,
			NexusAddr:    param.NexusAddr,
			NexusAccount: param.NexusAccount,
			NexusPwd:     param.NexusPwd,
			NexusRepo:    param.NexusRepo,
			CreatedUser:  middleware.GetRequestUser(c),
		}
		if err = database.AddTransExportCustomer(c, exportCustomer); err != nil {
			middleware.ReturnError(c, err)
			return
		}
	}
	middleware.ReturnSuccess(c)
}

func QueryExportCustomerList(c *gin.Context) {
	var result []*models.DataTransExportCustomerTable
	var err error
	if result, err = database.GetTransExportCustomerList(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, result)
}

func DeleteExportCustomer(c *gin.Context) {
	id := c.Query("id")
	var err error
	if strings.TrimSpace(id) == "" {
		middleware.ReturnError(c, fmt.Errorf("id is empty"))
		return
	}
	if err = database.DeleteTransExportCustomer(c, id); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnSuccess(c)
}

func GetExportNexusInfo(c *gin.Context) {
	var transDataVariableConfig *models.TransDataVariableConfig
	var err error
	if transDataVariableConfig, err = database.GetDataTransVariableMap(c); err != nil {
		middleware.ReturnError(c, err)
		return
	}
	middleware.ReturnData(c, transDataVariableConfig)
}
