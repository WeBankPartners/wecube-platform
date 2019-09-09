package com.webank.wecube.core.service;

import com.webank.wecube.core.commons.ApplicationProperties;
import com.webank.wecube.core.support.cmdb.CmdbServiceV2Stub;
import com.webank.wecube.core.dto.ci.ResourceTreeDto;
import com.webank.wecube.core.support.cmdb.dto.v2.ZoneLinkDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.webank.wecube.core.support.cmdb.dto.v2.PaginationQuery.defaultQueryObject;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
public class CmdbResourceServiceIntegrationTest {

    @Autowired
    private CmdbResourceService cmdbResourceService;
    @Autowired
    private CmdbServiceV2Stub cmdbServiceV2Stub;
    @Autowired
    ApplicationProperties.CmdbDataProperties cmdbDataProperties;

    @Test
    public void getAllIdcDesignTreesTest() {
        List<ResourceTreeDto> res = cmdbResourceService.getAllIdcDesignTrees();
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfIdcDesign(), defaultQueryObject()).getContents();
        assertThat(res.size()).isEqualTo(ciDatas.size());

    }

    @Test
    public void getAllIdcTreesTest() {
        List<ResourceTreeDto> res = cmdbResourceService.getAllIdcImplementTrees();
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfIdc(), defaultQueryObject()).getContents();
        assertThat(res.size()).isEqualTo(ciDatas.size());

    }

    @Test
    public void getAllZoneLinkDesignGroupByIdcDesignTest() {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZoneLinkDesign(), defaultQueryObject()).getContents();
        if (ciDatas.size() != 0) {
            List<ZoneLinkDto> linkedZoneLinkDesigns = cmdbResourceService.getAllZoneLinkDesignGroupByIdcDesign();
            assertThat(linkedZoneLinkDesigns.size()).isGreaterThan(0);
        }
    }

    @Test
    public void getAllZoneLinkGroupByIdcTest() {
        List<Object> ciDatas = cmdbServiceV2Stub.queryCiData(cmdbDataProperties.getCiTypeIdOfZoneLink(), defaultQueryObject()).getContents();
        if (ciDatas.size() != 0) {
            List<ZoneLinkDto> linkedZoneLinks = cmdbResourceService.getAllZoneLinkGroupByIdc();
            assertThat(linkedZoneLinks.size()).isGreaterThan(0);
        }
    }

}
