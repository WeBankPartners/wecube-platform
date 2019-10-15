package com.webank.wecube.platform.auth.server.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

//TODO
public class GZIPUtils {
    public static byte[] gZip(byte[] data) {
        byte[] b = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.finish();
            gzip.close();
            b = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            throw new RuntimeException();
        }
        return b;
    }

    public static byte[] unGZip(byte[] data) {
        byte[] b = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        GZIPInputStream gzip;
        try {
            gzip = new GZIPInputStream(bis);
            byte[] buf = new byte[1024];
            int num = -1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, num);
            }
            b = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bis.close();

            return b;
        } catch (IOException e) {
            throw new RuntimeException();
        }
        
    }
}