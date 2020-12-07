package com.webank.wecube.platform.core.controller;

import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.webank.wecube.platform.core.dto.plugin.DmeDto;

@Ignore
public class DataModelControllerTest extends AbstractControllerTest {

    @Autowired
    private DataModelController dataModelController;

    @Before
    public void setup() {
        mvc = MockMvcBuilders.standaloneSetup(dataModelController).build();
    }

    @Test
    public void getAllEntityByDmeShouldSucceed() throws Exception {
        DmeDto requestDmeDto = new DmeDto(
                "wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design");
        mvc.perform(post("/v1/data-model/dme/all-entities").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(requestDmeDto))).andExpect(status().isOk()).andDo(print())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].packageName", hasItem("wecmdb")))
                .andExpect(
                        jsonPath("$.data.[*].entityName", contains("subsys", "unit", "unit_design", "subsys_design")))
                .andReturn().getResponse();
    }

    @Test
    public void getAllEntityByDmeShouldSucceed1() throws Exception {
        DmeDto requestDmeDto = new DmeDto("wecmdb:subsys");
        mvc.perform(post("/v1/data-model/dme/all-entities").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(requestDmeDto))).andExpect(status().isOk()).andDo(print())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].packageName", hasItem("wecmdb")))
                .andExpect(jsonPath("$.data.[*].entityName", contains("subsys"))).andReturn().getResponse();
    }

    @Test
    public void getAllEntityByDmeShouldSucceed2() throws Exception {
        DmeDto requestDmeDto = new DmeDto("wecmdb:unit.unit_design>wecmdb:unit_design");
        mvc.perform(post("/v1/data-model/dme/all-entities").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(requestDmeDto))).andExpect(status().isOk()).andDo(print())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].packageName", hasItem("wecmdb")))
                .andExpect(jsonPath("$.data.[*].entityName", contains("unit", "unit_design"))).andReturn()
                .getResponse();
    }

    @Test
    public void getAllEntityByDmeShouldSucceed3() throws Exception {
        DmeDto requestDmeDto = new DmeDto(
                "wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design");
        mvc.perform(post("/v1/data-model/dme/all-entities").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(requestDmeDto))).andExpect(status().isOk()).andDo(print())
                .andExpect(jsonPath("$.status", is("OK")))
                .andExpect(jsonPath("$.data.[*].packageName", hasItem("wecmdb")))
                .andExpect(jsonPath("$.data.[*].entityName", contains("subsys", "unit", "unit_design"))).andReturn()
                .getResponse();
    }

}
