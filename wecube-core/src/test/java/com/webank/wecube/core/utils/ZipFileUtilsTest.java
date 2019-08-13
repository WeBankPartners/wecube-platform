package com.webank.wecube.core.utils;

import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;


public class ZipFileUtilsTest {


    @Test
    public void testUnzip() throws IOException {
        InputStream inputStream = Resources.getResource("zip-file-sample.zip").openStream();
        Map<ZipEntry, byte[]> unzippedFiles = ZipFileUtils.unzip(inputStream);
        assertThat(unzippedFiles.keySet())
                .extracting("name")
                .containsExactly("directory-sample/file-sample.txt", "file-sample.xml");
        assertThat(unzippedFiles.values())
                .containsExactly("file-content-sample".getBytes(), "<xml>xml content sample</xml>".getBytes());
    }
}