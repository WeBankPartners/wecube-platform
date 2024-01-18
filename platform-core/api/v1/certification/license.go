package certification

import (
	"bytes"
	"compress/zlib"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"github.com/gin-gonic/gin"
)

func UnmarshalWeLicense(data []byte) (result *models.WeLicense, err error) {
	zipReader, errReader := zlib.NewReader(bytes.NewReader(data))
	if errReader != nil {
		err = errReader
		return
	}
	defer zipReader.Close()
	// 读取解压缩后的数据
	decompressedData, errRead := ioutil.ReadAll(zipReader)
	if errRead != nil {
		err = errRead
		return
	}
	result = &models.WeLicense{}
	errJson := json.Unmarshal(decompressedData, result)
	if errJson != nil {
		err = errJson
		return
	}
	return
}

func MarshalWeLicense(lic *models.WeLicense) (data []byte, err error) {
	jsonData, errJson := json.Marshal(lic)
	if errJson != nil {
		err = errJson
		return
	}
	var compressedData bytes.Buffer
	zipWriter := zlib.NewWriter(&compressedData)
	defer zipWriter.Close()
	_, errWrite := zipWriter.Write(jsonData)
	if errWrite != nil {
		err = errWrite
		return
	}
	data = compressedData.Bytes()
	return
}

// License列表查询
func GetCertifications(c *gin.Context) {
	result, err := database.GetCertifications(c)
	if err != nil {
		middleware.ReturnError(c, err)
	} else {
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
	lic, err := UnmarshalWeLicense(fileBytes)
	if err != nil {
		middleware.ReturnError(c, err)
		return
	}
	// 写数据库
	var cert *models.PluginCertification
	cert, err = database.CreateCertification(c, lic, c.GetString(models.ContextUserId))
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
			middleware.ReturnError(c, fmt.Errorf("certification %d not found", certId))
		}
		exportData, err := MarshalWeLicense(&models.WeLicense{
			Plugin:      result.Plugin,
			PK:          result.Lpk,
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
