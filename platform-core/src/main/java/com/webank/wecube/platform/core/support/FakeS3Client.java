package com.webank.wecube.platform.core.support;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.webank.wecube.platform.core.commons.WecubeCoreException;

import java.io.File;
import java.io.IOException;

public class FakeS3Client implements S3Client{
    public static final String FILE_EXISTS = "exists";
    public static final String FAKE_S3_URL = "https://localhost:9000/s3/";

    @Override
    public boolean fileExists(String bucketName, String fileName) {
        return FILE_EXISTS.equalsIgnoreCase(fileName);
    }

    @Override
    public String uploadBinaryToS3(String bucketName, String s3keyName, byte[] binary) throws WecubeCoreException, IOException {
        return FAKE_S3_URL + bucketName + "/" + s3keyName;
    }

    @Override
    public String uploadFile(String bucketName, String s3KeyName, File file) {
        return FAKE_S3_URL + bucketName + "/" + s3KeyName;
    }

    @Override
    public S3ObjectInputStream downFile(String bucketName, String key) {
        return null;
    }

    @Override
    public void downFile(String bucketName, String key, String localPath) {

    }

    @Override
    public String getUrlFromS3(String bucketName, String s3KeyName) {
        return FAKE_S3_URL + bucketName + "/" + s3KeyName;
    }
}
