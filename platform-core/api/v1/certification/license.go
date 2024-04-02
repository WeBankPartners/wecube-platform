package certification

import (
	"fmt"
	"net/http"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

// License列表查询
func GetCertifications(c *gin.Context) {
	result, err := database.GetCertifications(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		for _, r := range result {
			r.Lpk = ""
			r.Signature = ""
			r.EncryptData = ""
		}
		middleware.ReturnData(c, result)
	}
}

// 导入License文件
func ImportCertification(c *gin.Context) {
	// 接收插件zip文件
	_, fileBytes, err := middleware.ReadFormFile(c, "uploadFile")
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	lic, err := database.UnmarshalWeLicense(fileBytes)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 检查是新增还是更新,写数据库
	var cert *models.PluginCertification
	existCert, err := database.GetSingleCertificationByName(c, lic.Plugin)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	if existCert != nil {
		cert, err = database.UpdateCertification(c, existCert.Id, lic, c.GetString(models.ContextUserId))
		cert.CreatedBy = existCert.CreatedBy
		cert.CreatedTime = existCert.CreatedTime
	} else {
		cert, err = database.CreateCertification(c, lic, c.GetString(models.ContextUserId))
	}
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, cert)
	}
}

// 导出License文件
func ExportCertification(c *gin.Context) {
	certId := c.Param("certId")
	result, err := database.GetSingleCertification(c, certId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		if result == nil {
			middleware.ReturnError(c, fmt.Errorf("certification %s not found", certId))
			return
		}
		exportData, err := database.MarshalWeLicense(&models.WeLicense{
			Plugin:      result.Plugin,
			Lpk:         result.Lpk,
			Data:        result.EncryptData,
			Signature:   result.Signature,
			Description: result.Description,
		})
		if err != nil {
			middleware.ReturnError(c, err)
			return
		}
		c.Status(http.StatusOK)
		c.Header("Content-Disposition", fmt.Sprintf("attachment;filename=%s-%s.WeLic", result.Plugin, time.Now().Format("20060102150405")))
		c.Header("Content-Type", "application/octet-stream")
		c.Header("Content-Length", fmt.Sprintf("%d", len(exportData)))
		c.Writer.Write(exportData)
	}
}

// 删除License
func DeleteCertification(c *gin.Context) {
	certId := c.Param("certId")
	err := database.DeleteCertification(c, certId)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
		middleware.ReturnData(c, nil)
	}
}
