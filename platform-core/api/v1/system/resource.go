package system

import (
	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

func QueryResourceServer(c *gin.Context) {
	var param models.QueryRequestParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.QueryResourceServer(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetResourceItemTypes(c *gin.Context) {
	data := []string{"s3_bucket", "mysql_database", "docker_container", "docker_image"}
	middleware.ReturnData(c, data)
}

func GetResourceItemStatus(c *gin.Context) {
	data := []string{"created", "running", "stopped"}
	middleware.ReturnData(c, data)
}

func QueryResourceItem(c *gin.Context) {
	var param models.QueryRequestParam
	if err := c.ShouldBindJSON(&param); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	result, err := database.QueryResourceItem(c, &param)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, result)
	}
}

func GetResourceServerStatus(c *gin.Context) {
	data := []string{"active", "inactive"}
	middleware.ReturnData(c, data)
}

func GetResourceServerTypes(c *gin.Context) {
	data := []string{"s3", "mysql", "docker"}
	middleware.ReturnData(c, data)
}

func CreateResourceServer(c *gin.Context) {
	var params []*models.ResourceServer
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := database.CreateResourceServer(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, params)
	}
}

func UpdateResourceServer(c *gin.Context) {
	var params []*models.ResourceServer
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := database.UpdateResourceServer(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, params)
	}
}

func DeleteResourceServer(c *gin.Context) {
	var params []*models.ResourceServer
	if err := c.ShouldBindJSON(&params); err != nil {
		middleware.ReturnError(c, exterror.Catch(exterror.New().RequestParamValidateError, err))
		return
	}
	err := database.DeleteResourceServer(c, params)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnSuccess(c)
	}
}

func GetResourceServerSerialNum(c *gin.Context) {
	//resourceServerId := c.Param("resourceServerId")

}
