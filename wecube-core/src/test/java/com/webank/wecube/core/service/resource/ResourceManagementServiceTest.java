package com.webank.wecube.core.service.resource;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.webank.wecube.core.DatabaseBasedTest;
import com.webank.wecube.core.commons.WecubeCoreException;
import com.webank.wecube.core.dto.QueryRequest;
import com.webank.wecube.core.dto.QueryResponse;
import com.webank.wecube.core.dto.ResourceServerDto;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResourceManagementServiceTest extends DatabaseBasedTest {
    @Autowired
    private ResourceManagementService service;

    @Autowired
    RestTemplate restTemplate;

    @Before
    public void initMock() throws URISyntaxException {
        initMockCmdb();
    }

    @Test
    public void whenCreateS3ServerWithValidValuesShouldSuccess() {
        List<ResourceServerDto> resourceServers = new ArrayList<>();
        resourceServers.add(newValidServer());
        service.createServers(resourceServers);

        QueryResponse<ResourceServerDto> response = service.retrieveServers(new QueryRequest());
        assertEquals(response.getContents().size(), 1);
        assertEquals(response.getContents().get(0).getName(), "wecube_s3_testing_server");
    }

    @Test
    public void whenDeleteUnAllocatedAS3ServerShouldSuccess() throws Exception {
        List<ResourceServerDto> resourceServers = new ArrayList<>();
        resourceServers.add(newValidServer());

        List<ResourceServerDto> servers = service.createServers(resourceServers);

        QueryResponse<ResourceServerDto> response = service.retrieveServers(new QueryRequest());
        assertEquals(response.getContents().size(), 1);
        assertEquals(response.getContents().get(0).getName(), "wecube_s3_testing_server");

        servers.forEach(server -> {
            server.setIsAllocated(false);
        });

        service.updateServers(servers);
        service.deleteServers(servers);

        response = service.retrieveServers(new QueryRequest());
        assertEquals(response.getContents().size(), 0);
    }

    private void initMockCmdb() throws URISyntaxException {
        MockRestServiceServer mockCmdbRunningServer = MockRestServiceServer.bindTo(restTemplate).build();
        mockCmdbRunningServer.expect(manyTimes(), requestTo(new URI("http://cmdb-server-ip:37000/cmdb/api/v2/enum/cats/retrieve")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"statusCode\": \"OK\",\"data\": {\"contents\":[{\"catId\":\"81\",\"catName\":\"security\"}]}}", MediaType.APPLICATION_JSON));

        mockCmdbRunningServer.expect(manyTimes(), requestTo(new URI("http://cmdb-server-ip:37000/cmdb/api/v2/enum/codes/retrieve")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"statusCode\": \"OK\",\"data\": {\"contents\":[{\"code\":\"seed\",\"value\":\"seed123456\"}]}}", MediaType.APPLICATION_JSON));
    }

    @Test(expected = WecubeCoreException.class)
    public void whenDeleteAllocatedAS3ServerShouldFail() {
        List<ResourceServerDto> resourceServers = new ArrayList<>();
        resourceServers.add(newValidServer());
        List<ResourceServerDto> servers = service.createServers(resourceServers);

        QueryResponse<ResourceServerDto> response = service.retrieveServers(new QueryRequest());
        assertEquals(response.getContents().size(), 1);
        assertEquals(response.getContents().get(0).getName(), "wecube_s3_testing_server");

        servers.forEach(server -> {
            server.setIsAllocated(true);
        });

        service.updateServers(servers);
        service.deleteServers(servers);
    }

    @Test(expected = ConstraintViolationException.class)
    public void whenCreateS3ServerWithoutRequiredFieldHostShouldFail() {
        List<ResourceServerDto> resourceServers = new ArrayList<>();
        ResourceServerDto dto = newValidServer();
        dto.setHost(null);
        resourceServers.add(dto);
        service.createServers(resourceServers);
    }

    @Test(expected = WecubeCoreException.class)
    public void whenCreateS3ServerWithInvalidServerTypeShouldFail() {
        List<ResourceServerDto> resourceServers = new ArrayList<>();
        ResourceServerDto dto = newValidServer();
        dto.setType("invalid_server_type");
        resourceServers.add(dto);
        service.createServers(resourceServers);
    }

    private ResourceServerDto newValidServer() {
        ResourceServerDto dto = new ResourceServerDto();
        dto.setHost("***REMOVED***");
        dto.setPort("9000");
        dto.setLoginUsername("access_123");
        dto.setLoginPassword("secret_123");
        dto.setName("wecube_s3_testing_server");
        dto.setPurpose("wecube testing s3 server");
        dto.setType(ResourceServerType.S3.getCode());
        return dto;
    }
}
