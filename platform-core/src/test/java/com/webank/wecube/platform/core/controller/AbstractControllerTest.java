package com.webank.wecube.platform.core.controller;

import com.webank.wecube.platform.core.DatabaseBasedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public abstract class AbstractControllerTest extends DatabaseBasedTest {

    @Autowired
    protected MockMvc mvc;

}
