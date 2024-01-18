package database

import (
	"context"
	"time"

	"github.com/WeBankPartners/go-common-lib/guid"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

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
		Lpk:         lic.PK,
		EncryptData: lic.Data,
		Signature:   lic.Signature,
		Description: lic.Description,
		CreatedTime: time.Now(),
		CreatedBy:   userId,
		UpdatedTime: time.Now(),
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
