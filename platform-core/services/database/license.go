package database

import (
	"bytes"
	"compress/zlib"
	"context"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"math/rand"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/tools"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
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
	_, errWrite := zipWriter.Write(jsonData)
	if errWrite != nil {
		err = errWrite
		return
	}
	errWrite = zipWriter.Close()
	if errWrite != nil {
		err = errWrite
		return
	}
	data = compressedData.Bytes()
	return
}

func generateRandKey(length int) (key, iv string) {
	rand.Seed(time.Now().UnixNano())
	// 定义字母数字字符集
	charset := "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	// 生成随机字符串
	keyByte := make([]byte, length)
	ivByte := make([]byte, length)
	for i := 0; i < length; i++ {
		keyByte[i] = charset[rand.Intn(len(charset))]
		ivByte[i] = charset[rand.Intn(len(charset))]
	}
	key = string(keyByte)
	iv = string(ivByte)
	return
}

/**
* Func: GeneratePluginEnv 用于生成启动插件时自动生成LICENSE_PK & LICENSE_DATA & LICENSE_SIGNATURE & LICENSE_CODE环境变量值
	* @params subsystemPubKey 子系统公钥(可选，若不提供，则必须提供子系统私钥)
	* @params subsystemPriKey 子系统私钥(可选，若不提供，则必须提供子系统公钥)
	* @params pluginName 插件名称，必须提供，如果找不到插件的License配置，则返回的环境变量值都是空值
* @return LICENSE_CODE, LICENSE_PK, LICENSE_DATA, LICENSE_SIGNATURE环境变量对应的值
*/
func GeneratePluginEnv(subsystemPubKey, subsystemPriKey, pluginName string) (lic_code, lic_pk, lic_data, lic_sign string, err error) {
	cert, certErr := GetSingleCertificationByName(context.Background(), pluginName)
	if certErr != nil {
		err = certErr
		return
	}
	if cert == nil {
		return
	}
	s16Key, s16IV := generateRandKey(16)
	oriLicCode := s16Key + s16IV
	lic_pk, err = tools.AESCBCEncode(oriLicCode, []byte(cert.Lpk))
	if err != nil {
		return
	}
	lic_data, err = tools.AESCBCEncode(oriLicCode, []byte(cert.EncryptData))
	if err != nil {
		return
	}
	lic_sign, err = tools.AESCBCEncode(oriLicCode, []byte(cert.Signature))
	if err != nil {
		return
	}
	if subsystemPubKey == "" {
		if subsystemPriKey == "" {
			err = fmt.Errorf("must provide public key or private key")
			return
		}
		subsystemPubKey, err = tools.RSAExtractPubKey(subsystemPriKey)
		if err != nil {
			return
		}
	}
	lic_code, err = tools.RSAEncrypt([]byte(oriLicCode), subsystemPubKey)
	if err != nil {
		return
	}
	return
}

func GetCertifications(ctx context.Context) (result []*models.PluginCertification, err error) {
	err = db.MysqlEngine.Context(ctx).Table(new(models.PluginCertification)).Desc("updated_time").Find(&result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	return
}

func GetSingleCertification(ctx context.Context, certId string) (result *models.PluginCertification, err error) {
	result = &models.PluginCertification{}
	var exists bool
	exists, err = db.MysqlEngine.Context(ctx).Table(new(models.PluginCertification)).Where("id = ?", certId).Get(result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		result = nil
		return
	}
	return
}

func DeleteCertification(ctx context.Context, certId string) (err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	_, err = session.Table(new(models.PluginCertification)).Where("id = ?", certId).Delete()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func CreateCertification(ctx context.Context, lic *models.WeLicense, userId string) (cert *models.PluginCertification, err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	cert = &models.PluginCertification{
		Id:          guid.CreateGuid(),
		Plugin:      lic.Plugin,
		Lpk:         lic.Lpk,
		EncryptData: lic.Data,
		Signature:   lic.Signature,
		Description: lic.Description,
		CreatedTime: time.Now().Format("2006-01-02 15:04:05"),
		CreatedBy:   userId,
		UpdatedTime: time.Now().Format("2006-01-02 15:04:05"),
		UpdatedBy:   userId,
	}
	_, err = session.Insert(cert)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	return
}

func GetSingleCertificationByName(ctx context.Context, pluginName string) (result *models.PluginCertification, err error) {
	result = &models.PluginCertification{}
	var exists bool
	exists, err = db.MysqlEngine.Context(ctx).Table(new(models.PluginCertification)).Where("plugin = ?", pluginName).Get(result)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	if !exists {
		result = nil
		return
	}
	return
}

func UpdateCertification(ctx context.Context, certId string, lic *models.WeLicense, userId string) (cert *models.PluginCertification, err error) {
	session := db.MysqlEngine.NewSession().Context(ctx)
	defer session.Close()
	err = session.Begin()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	updateData := make(map[string]interface{})
	updatedTime := time.Now().Format("2006-01-02 15:04:05")
	updateData["lpk"] = lic.Lpk
	updateData["encrypt_data"] = lic.Data
	updateData["signature"] = lic.Signature
	updateData["description"] = lic.Description
	updateData["updated_time"] = updatedTime
	updateData["updated_by"] = userId
	_, err = session.Table(new(models.PluginCertification)).Where("id = ?", certId).Update(updateData)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	err = session.Commit()
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseExecuteError, err)
		return
	}
	cert = &models.PluginCertification{
		Id:          certId,
		Plugin:      lic.Plugin,
		Lpk:         lic.Lpk,
		EncryptData: lic.Data,
		Signature:   lic.Signature,
		Description: lic.Description,
		UpdatedTime: updatedTime,
		UpdatedBy:   userId,
	}
	return
}
