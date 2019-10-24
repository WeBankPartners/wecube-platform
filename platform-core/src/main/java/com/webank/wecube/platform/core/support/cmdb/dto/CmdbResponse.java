package com.webank.wecube.platform.core.support.cmdb.dto;


import lombok.Data;

import java.util.List;

@Data
public class CmdbResponse<DATATYPE> {
    public static final String STATUS_CODE_OK = "OK";

    private String statusCode;
    private String statusMessage;

    private DATATYPE data;

    public static class DefaultCmdbResponse extends CmdbResponse<Object> {}

    public static class IntegerCmdbResponse extends CmdbResponse<Integer> {}

    public static class ListDataResponse extends CmdbResponse<List> {}

}
