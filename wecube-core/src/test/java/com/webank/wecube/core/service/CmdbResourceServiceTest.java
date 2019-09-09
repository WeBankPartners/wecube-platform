package com.webank.wecube.core.service;

import com.alibaba.fastjson.JSONObject;
import com.webank.wecube.core.dto.ci.ResourceTreeDto;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.support.cmdb.dto.v2.CiTypeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.ZoneLinkDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class CmdbResourceServiceTest {

    @Autowired
    CmdbResourceService cmdbResourceService;
    @Mock
    CmdbServiceV2Stub cmdbServiceV2Stub;

    @Before
    public void init() {
        cmdbResourceService.setCmdbServiceV2Stub(cmdbServiceV2Stub);
        when(cmdbServiceV2Stub.getCiType(22)).thenReturn(JSONObject.parseObject("{\n" +
                "        \"ciTypeId\": 22,\n" +
                "        \"catalogId\": 135,\n" +
                "        \"description\": \"mock机房设计\",\n" +
                "        \"layerId\": 5,\n" +
                "        \"name\": \"机房设计\",\n" +
                "        \"seqNo\": 1,\n" +
                "        \"tableName\": \"IDC_design\",\n" +
                "        \"status\": \"created\",\n" +
                "        \"imageFileId\": 22,\n" +
                "        \"layerCode\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        },\n" +
                "        \"catalogCode\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        },\n" +
                "        \"ciStateType\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        }\n" +
                "      }", CiTypeDto.class)

        );

        when(cmdbServiceV2Stub.getCiType(22)).thenReturn(JSONObject.parseObject("{\n" +
                "        \"ciTypeId\": 22,\n" +
                "        \"catalogId\": 135,\n" +
                "        \"description\": \"mock机房设计\",\n" +
                "        \"layerId\": 5,\n" +
                "        \"name\": \"机房设计\",\n" +
                "        \"seqNo\": 1,\n" +
                "        \"tableName\": \"IDC_design\",\n" +
                "        \"status\": \"created\",\n" +
                "        \"imageFileId\": 22,\n" +
                "        \"layerCode\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        },\n" +
                "        \"catalogCode\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        },\n" +
                "        \"ciStateType\": {\n" +
                "          \"cat\": {\n" +
                "            \"codes\": []\n" +
                "          }\n" +
                "        }\n" +
                "      }", CiTypeDto.class)

        );

    }

    @Test
    public void getAllIdcDesignTreesTest() {
        List<ResourceTreeDto> res = cmdbResourceService.getAllIdcDesignTrees();
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(22, defaultQueryObject()).getContents();
        assertThat(res.size()).isEqualTo(ciDatas.size());
    }

    @Ignore
    @Test
    public void getAllIdcTreesTest() {
        List<ResourceTreeDto> res = cmdbResourceService.getAllIdcImplementTrees();
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(16, defaultQueryObject()).getContents();
        assertThat(res.size()).isEqualTo(ciDatas.size());

    }

    @Ignore
    @Test
    public void getAllZoneLinkDesignGroupByIdcDesignTest() {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(24, defaultQueryObject()).getContents();
        if (ciDatas.size() != 0) {
            List<ZoneLinkDto> linkedZoneLinkDesigns = cmdbResourceService.getAllZoneLinkDesignGroupByIdcDesign();
            assertThat(linkedZoneLinkDesigns.size()).isGreaterThan(0);
        }
    }

    @Ignore
    @Test
    public void getAllZoneLinkGroupByIdcTest() {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(18, defaultQueryObject()).getContents();
        if (ciDatas.size() != 0) {
            List<ZoneLinkDto> linkedZoneLinks = cmdbResourceService.getAllZoneLinkGroupByIdc();
            assertThat(linkedZoneLinks.size()).isGreaterThan(0);
        }
    }

}
