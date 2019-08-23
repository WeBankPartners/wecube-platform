package com.webank.wecube.core.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.isA;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.core.commons.WecubeCoreException;

public class ApiProxyControllerTest extends AbstractControllerTest {

    @Autowired
    ApiProxyController apiProxyController;

    @Autowired
    RestTemplate restTemplate;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testPluginApiProxy() throws Exception {
        mockPluginPackageWithRunningInstance();

        String testApi = "/api-proxy/pluginABC/api/v1/users";

        MockMvc mockMvc = standaloneSetup(apiProxyController).defaultRequest(get(testApi).servletPath(testApi).accept(MediaType.APPLICATION_JSON)).build();

        MockRestServiceServer mockPluginRunningServer = MockRestServiceServer.bindTo(restTemplate).build();

        mockPluginRunningServer.expect(once(), requestTo(new URI("http://10.0.0.99:8080/api/v1/users")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("Mock message from plugin service.", MediaType.APPLICATION_JSON));

        mockMvc.perform(get(testApi).contentType(MediaType.APPLICATION_JSON).content("Mock message from front-end."))
                .andExpect(content().string("Mock message from plugin service."));

        mockPluginRunningServer.verify();
    }

    @Test
    public void testPluginApiProxyWhenNoAvailablePluginInstanceFound() throws Exception {
        mockPluginPackageWithRunningInstance();

        String testApi = "/api-proxy/pluginXYZ/api/v1/users";

        exceptionRule.expectCause(isA(WecubeCoreException.class));
        exceptionRule.expectMessage(containsString("No instance for plugin [pluginXYZ] is available."));

        MockMvc mockMvc = standaloneSetup(apiProxyController).defaultRequest(get(testApi).servletPath(testApi).accept(MediaType.APPLICATION_JSON)).build();

        mockMvc.perform(get(testApi).contentType(MediaType.APPLICATION_JSON).content("Mock message from front-end."));
    }

    private void mockPluginPackageWithRunningInstance() {
        executeSql("insert into plugin_packages (id, name, version) values\n" +
                " (4, 'pluginABC', 'v1.0')\n" +
                ",(5, 'pluginXYZ', 'v1.0')\n" +
                ";\n" +
                "insert into plugin_instances (id, instance_container_id, package_id, host, port, status) values\n" +
                " (41, 'mock-instance_container_id', 4, '10.0.0.99', 8080, 'RUNNING')\n" +
                ",(51, 'mock-instance_container_id', 5, '10.0.0.99', 8081, 'REMOVED')\n" +
                ";");
    }
}
