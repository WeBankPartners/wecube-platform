package bash

import (
	"context"
	"fmt"
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
			err = fmt.Errorf("upload file %s to s3 fail,%s ", k, putErr.Error())
			break
		}
	}
	return
}

func DownloadPackage() {

}

func MakeBucket(bucket string) (err error) {
	minioClient, newErr := minio.New(models.Config.S3.ServerAddress, &minio.Options{Creds: credentials.NewStaticV4(models.Config.S3.AccessKey, models.Config.S3.SecretKey, "")})
	if newErr != nil {
		return fmt.Errorf("minio new client fail,%s ", newErr.Error())
	}
	if err = minioClient.MakeBucket(context.Background(), bucket, minio.MakeBucketOptions{}); err != nil {
		err = fmt.Errorf("new s3 bucket %s fail,%s ", bucket, err.Error())
	}
	return
}
