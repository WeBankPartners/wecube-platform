package com.webank.wecube.platform.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ZipFileUtils {

    public static Map<ZipEntry, byte[]> unzip(InputStream inputStream) throws IOException {
        Map<ZipEntry, byte[]> entryFiles = new HashMap<>();
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            if (!zipEntry.isDirectory()) {
                entryFiles.put(zipEntry, IOUtils.readFully(zis, (int) zipEntry.getSize()));
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return entryFiles;
    }


}
