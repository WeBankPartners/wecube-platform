package com.webank.wecube.platform.core.support;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.webank.wecube.platform.core.commons.WecubeCoreException;

public interface S3Client {
    boolean fileExists(String bucketName, String fileName);

    String uploadBinaryToS3(String bucketName, String s3keyName, byte[] binary) throws WecubeCoreException, IOException;

    String uploadFile(String bucketName, String s3KeyName, File file);

    S3ObjectInputStream downFile(String bucketName, String key);

    void downFile(String bucketName, String key, String localPath);

    String getUrlFromS3(String bucketName, String s3KeyName);
    
    List<S3ObjectSummary> listObjects(String bucketName);
}
