package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.dto.CommonResponseDto;
import com.webank.wecube.platform.core.dto.DmeDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.webank.wecube.platform.core.utils.JsonUtils.toJsonString;

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
        mvc.perform(get("/v1/data-model/dme/all-entities").contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(requestDmeDto))).andExpect(status().isOk()).andDo(print()).andReturn()
                .getResponse();
        assertThat(false);
    }

    @Test
    public void getLastEntityByDmeShouldSucceed() {
        int TARGET_ENTITY_SIZE = 4;

        try {
            mvc.perform(get("/v1/packages/wecmdb/entities/system_design/retrieve").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status", is(CommonResponseDto.STATUS_OK)))
                    .andExpect(jsonPath("$.data", is(iterableWithSize(TARGET_ENTITY_SIZE))))
                    .andExpect(jsonPath("$.data[*].id", containsInAnyOrder("0001_0000000001", "0001_0000000002",
                            "0001_0000000003", "0001_0000000004")))
                    .andDo(print()).andReturn().getResponse();
        } catch (Exception e) {
            fail("Failed to fetch target entity: " + e.getMessage());
        }
    }

}
