package com.webank.wecube.platform.core.support;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class RealS3Client implements S3Client {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private AmazonS3 s3Client;
    private String endpoint;

    public AmazonS3 getS3Client() {
        return this.s3Client;
    }

    public RealS3Client(String endpoint, String accessKey, String secretKey) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("AWSS3V4SignerType");
        AmazonS3 newClient = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, Regions.US_EAST_1.name()))
                .withPathStyleAccessEnabled(true)
                .withClientConfiguration(clientConfiguration)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        this.s3Client = newClient;
        this.endpoint = endpoint;
    }

    @Override
    public boolean fileExists(String bucketName, String fileName) {
        ObjectListing objects = s3Client.listObjects(bucketName);

        for (S3ObjectSummary ob : objects.getObjectSummaries()) {
            if (ob.getKey() == fileName) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String uploadBinaryToS3(String bucketName, String s3keyName, byte[] binary) throws WecubeCoreException, IOException {
        String urlStr = endpoint + "/" + bucketName + "/" + s3keyName;

        File tempFile = File.createTempFile("temp", ",tmp", null);

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(binary);
            uploadFile(bucketName, s3keyName, tempFile);
            if (!tempFile.delete()) {
                throw new WecubeCoreException("3293","Delete temporary file failed");
            }
        } catch (IOException e) {
            log.error("upload file to S3/MinIO meet error: ", e);
        }
        return urlStr;
    }

    @Override
    public String uploadFile(String bucketName, String s3KeyName, File file) {
        if (!(s3Client.doesBucketExist(bucketName))) {
            s3Client.createBucket(new CreateBucketRequest(bucketName));
        }

        if (fileExists(bucketName, s3KeyName)) {
            String msg = String.format("File[%s] already exists.", s3KeyName);
            throw new WecubeCoreException("3292",msg,s3KeyName);
        }

        s3Client.putObject(new PutObjectRequest(bucketName, s3KeyName, file).withCannedAcl(CannedAccessControlList.Private));
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, s3KeyName);
        URL url = s3Client.generatePresignedUrl(urlRequest);

        log.info("uploaded File  [{}] to S3. url = [{}]", file.getAbsolutePath(), url);
        return url.toString();
    }

    @Override
    public S3ObjectInputStream downFile(String bucketName, String key) {
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        S3Object object = s3Client.getObject(request);
        S3ObjectInputStream inputStream = object.getObjectContent();
        log.info("downloaded file [{}] from s3 , url {} , ", key, inputStream.getHttpRequest().getURI());
        return inputStream;
    }

    @Override
    public void downFile(String bucketName, String key, String localPath) {
        GetObjectRequest request = new GetObjectRequest(bucketName, key);
        s3Client.getObject(request, new File(localPath));
    }

    @Override
    public String getUrlFromS3(String bucketName, String s3KeyName) {
        GeneratePresignedUrlRequest httpRequest = new GeneratePresignedUrlRequest(bucketName, s3KeyName);
        String url = s3Client.generatePresignedUrl(httpRequest).toString();//临时链接
        return url;
    }

    @Override
    public List<S3ObjectSummary> listObjects(String bucketName) {
        ObjectListing objectListing = s3Client.listObjects(bucketName);
        return objectListing.getObjectSummaries();
    }
}
