package bash

import (
	"context"
	"fmt"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

func UploadPluginPackage(bucket string, fileMap map[string]string) (err error) {
	minioClient, newErr := minio.New(models.Config.S3.ServerAddress, &minio.Options{Creds: credentials.NewStaticV4(models.Config.S3.AccessKey, models.Config.S3.SecretKey, "")})
	if newErr != nil {
		return fmt.Errorf("minio new client fail,%s ", newErr.Error())
	}
	for k, v := range fileMap {
		_, putErr := minioClient.FPutObject(context.Background(), bucket, v, k, minio.PutObjectOptions{ContentType: "application/octet-stream"})
		if putErr != nil {
			err = fmt.Errorf("upload file %s to s3 %s fail,%s ", k, v, putErr.Error())
			break
		}
	}
	return
}

func DownloadPackageFile(bucket, key string) (tmpPath string, err error) {
	var fileDir, fileName string
	if fileDir, err = newTmpDir(); err != nil {
		return
	}
	fileName = key
	if lastIndex := strings.LastIndex(key, "/"); lastIndex >= 0 {
		fileName = key[lastIndex+1:]
	}
	tmpPath = fmt.Sprintf("%s/%s", fileDir, fileName)
	minioClient, newErr := minio.New(models.Config.S3.ServerAddress, &minio.Options{Creds: credentials.NewStaticV4(models.Config.S3.AccessKey, models.Config.S3.SecretKey, "")})
	if newErr != nil {
		return tmpPath, fmt.Errorf("minio new client fail,%s ", newErr.Error())
	}
	if err = minioClient.FGetObject(context.Background(), bucket, key, tmpPath, minio.GetObjectOptions{Checksum: true}); err != nil {
		err = fmt.Errorf("download s3 file %s to path:%s fail,%s ", key, tmpPath, err.Error())
	}
	return
}

func MakeBucket(bucket string) (err error) {
	minioClient, newErr := minio.New(models.Config.S3.ServerAddress, &minio.Options{Creds: credentials.NewStaticV4(models.Config.S3.AccessKey, models.Config.S3.SecretKey, "")})
	if newErr != nil {
		return fmt.Errorf("minio new client fail,%s ", newErr.Error())
	}
	exists, errExists := minioClient.BucketExists(context.Background(), bucket)
	if errExists != nil {
		err = fmt.Errorf("check s3 bucket %s fail,%s ", bucket, errExists.Error())
		return
	}
	if !exists {
		if err = minioClient.MakeBucket(context.Background(), bucket, minio.MakeBucketOptions{}); err != nil {
			err = fmt.Errorf("new s3 bucket %s fail,%s ", bucket, err.Error())
		}
	}
	return
}
