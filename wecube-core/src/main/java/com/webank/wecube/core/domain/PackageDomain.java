package com.webank.wecube.core.domain;

import lombok.Data;

import java.util.List;

@Data
public class PackageDomain {
    private List<String> configFilesWithPath;
    private String deployFile;
    private String startFile;
    private String stopFile;

}
