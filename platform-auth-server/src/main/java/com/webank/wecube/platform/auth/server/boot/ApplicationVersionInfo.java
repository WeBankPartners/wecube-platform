package com.webank.wecube.platform.auth.server.boot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApplicationVersionInfo {
    private String version = "2.6.1";
    
    private Resource versionInfoFile;
    
    public ApplicationVersionInfo(@Value("classpath:version.inf") Resource versionInfoFile) throws IOException{
        this.versionInfoFile = versionInfoFile;
        if((this.versionInfoFile == null)){
            throw new IllegalArgumentException();
        }
        
        if(!this.versionInfoFile.exists()){
            throw new IllegalArgumentException();
        }
        
        InputStream input = this.versionInfoFile.getInputStream();
        String versionStr = IOUtils.toString(input, Charset.forName("UTF-8"));
        if(StringUtils.isNoneBlank(versionStr)){
            this.version = versionStr;
        }else{
            throw new IllegalArgumentException();
        }
    }

    public String getVersion() {
        return version;
    }
    
    
}
