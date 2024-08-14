package bash

import (
	"context"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"os"
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

type PlatformObjectInfo []string

func ListBucketFiles(bucket string) (datas []PlatformObjectInfo, err error) {
	minioClient, newErr := minio.New(models.Config.S3.ServerAddress, &minio.Options{Creds: credentials.NewStaticV4(models.Config.S3.AccessKey, models.Config.S3.SecretKey, "")})
	if newErr != nil {
		return nil, fmt.Errorf("minio new client fail,%s ", newErr.Error())
	}
	datas = make([]PlatformObjectInfo, 0)
	for obj := range minioClient.ListObjects(context.Background(), bucket, minio.ListObjectsOptions{Recursive: true, MaxKeys: 200}) {
		data := make(PlatformObjectInfo, 0)
		fileName := ""
		filePath := ""
		parts := strings.Split(obj.Key, "/")
		if len(parts) == 1 {
			fileName = parts[0]
		} else {
			fileName = parts[len(parts)-1]
			filePath = strings.Join(parts[:len(parts)-1], "/") + "/"
		}
		data = append(data, fileName)
		data = append(data, filePath)
		data = append(data, obj.ETag)
		data = append(data, obj.LastModified.UTC().Format("2006-01-02T15:04:05Z"))
		datas = append(datas, data)
	}
	return
}

func RemoveTmpFile(tmpFile string) {
	if removeFileErr := os.RemoveAll(tmpFile); removeFileErr != nil {
		log.Logger.Error("try to remove tmp file fail", log.String("file", tmpFile), log.Error(removeFileErr))
	}
}
