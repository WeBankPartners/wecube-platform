package com.webank.wecube.platform.core.service.datamodel;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.Ignore;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

@Ignore
public class RootlessExpressionServiceMocker {
	private String gatewayUrl;
	
	public RootlessExpressionServiceMocker(String gatewayUrl) {
		super();
		this.gatewayUrl = gatewayUrl;
	}
	
	public void mockPackageNameWithDashAndFwdNodeExpressionServerWithFilter(MockRestServiceServer server) {
		// mockFwdNodeExpression
		server.expect(ExpectedCount.once(),
				requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"key_name\": \"DEMO1\",\n"
						+ "            \"p_guid\": null,\n" + "            \"business_group\": \"business_group_A\",\n"
						+ "            \"code\": \"DEMO1\",\n" + "            \"r_guid\": \"0001_0000000001\",\n"
						+ "            \"name\": \"演示系统1\",\n" + "            \"description\": \"1\",\n"
						+ "            \"id\": \"0001_0000000001\",\n" + "            \"state\": \"new\",\n"
						+ "            \"fixed_date\": \"\"\n" + "        }\n]}", MediaType.APPLICATION_JSON));

	}

	public  void mockMultipleLinksWithMixedOpExpressionServer(MockRestServiceServer server) {

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD\",\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"nertonsong\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE PRD\",\n" +
                        "            \"id\": \"0007_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"key_name\": \"ECIF-CORE_DEV\",\n" +
                        "            \"code\": \"CORE_TWO\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"manager\": \"howechen\",\n" +
                        "            \"r_guid\": \"0007_0000000001\",\n" +
                        "            \"description\": \"ECIF-CORE DEV\",\n" +
                        "            \"id\": \"0007_0000000002\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"env\": 111,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:17\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.once(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"package\": \"\",\n" +
                        "            \"r_guid\": \"0008_0000000001\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000001\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:35\",\n" +
                        "            \"unit_design\": \"0003_0000000006\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": \"\",\n" +
                        "            \"code\": \"DB\",\n" +
                        "            \"orchestration\": 231,\n" +
                        "            \"package\": \"0011_0000000010\",\n" +
                        "            \"r_guid\": \"0008_0000000007\",\n" +
                        "            \"description\": \"aa\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000007\",\n" +
                        "            \"state\": 38,\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000007\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"package\": \"\",\n" +
                        "            \"r_guid\": \"0008_0000000001\",\n" +
                        "            \"description\": \"\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000002\",\n" +
                        "            \"state\": 38,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:30:35\",\n" +
                        "            \"unit_design\": \"0003_0000000007\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"biz_key\": \"\",\n" +
                        "            \"code\": \"DB\",\n" +
                        "            \"orchestration\": 231,\n" +
                        "            \"package\": \"0011_0000000010\",\n" +
                        "            \"r_guid\": \"0008_0000000007\",\n" +
                        "            \"description\": \"aa\",\n" +
                        "            \"resource_set\": \"0020_0000000001\",\n" +
                        "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" +
                        "            \"instance_num\": 1,\n" +
                        "            \"subsys\": \"0007_0000000001\",\n" +
                        "            \"id\": \"0008_0000000003\",\n" +
                        "            \"state\": 37,\n" +
                        "            \"fixed_date\": \"\",\n" +
                        "            \"unit_design\": \"0003_0000000006\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/unit_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"subsys_design\": \"0002_0000000010\",\n" +
                        "            \"code\": \"APP\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0003_0000000006\",\n" +
                        "            \"description\": \"Application module\",\n" +
                        "            \"resource_set_design\": null,\n" +
                        "            \"across_idc\": 147,\n" +
                        "            \"type\": 106,\n" +
                        "            \"key_name\": \"ECIF-CORE-APP\",\n" +
                        "            \"resource_set_design_type\": 136,\n" +
                        "            \"name\": \"Application module\",\n" +
                        "            \"id\": \"0003_0000000006\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:29:05\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));

        server.expect(ExpectedCount.manyTimes(), requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n" +
                        "    \"status\": \"OK\",\n" +
                        "    \"message\": \"Success\",\n" +
                        "    \"data\": [\n" +
                        "        {\n" +
                        "            \"biz_key\": null,\n" +
                        "            \"business_group\": 105,\n" +
                        "            \"code\": \"CORE\",\n" +
                        "            \"orchestration\": null,\n" +
                        "            \"r_guid\": \"0002_0000000010\",\n" +
                        "            \"description\": \"CRM Core Subsystem\",\n" +
                        "            \"dcn_design_type\": 135,\n" +
                        "            \"key_name\": \"ECIF-CORE\",\n" +
                        "            \"name\": \"CRM Core Subsystem\",\n" +
                        "            \"id\": \"0002_0000000010\",\n" +
                        "            \"state\": 34,\n" +
                        "            \"fixed_date\": \"2019-07-24 16:28:27\",\n" +
                        "            \"system_design\": \"0001_0000000003\"\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}", MediaType.APPLICATION_JSON));


    }
	
	public void mockMultipleLinksWithOpByOnlyExpressionServer(MockRestServiceServer server) {

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"subsys_design\": \"0002_0000000010\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD\",\n" + "            \"code\": \"CORE\",\n"
						+ "            \"orchestration\": null,\n" + "            \"manager\": \"nertonsong\",\n"
						+ "            \"r_guid\": \"0007_0000000001\",\n"
						+ "            \"description\": \"ECIF-CORE PRD\",\n"
						+ "            \"id\": \"0007_0000000001\",\n" + "            \"state\": 37,\n"
						+ "            \"env\": 111,\n" + "            \"fixed_date\": \"2019-07-24 16:30:17\"\n"
						+ "        },\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"subsys_design\": \"0002_0000000010\",\n"
						+ "            \"key_name\": \"ECIF-CORE_DEV\",\n" + "            \"code\": \"CORE_TWO\",\n"
						+ "            \"orchestration\": null,\n" + "            \"manager\": \"howechen\",\n"
						+ "            \"r_guid\": \"0007_0000000001\",\n"
						+ "            \"description\": \"ECIF-CORE DEV\",\n"
						+ "            \"id\": \"0007_0000000002\",\n" + "            \"state\": 37,\n"
						+ "            \"env\": 111,\n" + "            \"fixed_date\": \"2019-07-24 16:30:17\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.once(),
				requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"code\": \"APP\",\n" + "            \"orchestration\": null,\n"
						+ "            \"package\": \"\",\n" + "            \"r_guid\": \"0008_0000000001\",\n"
						+ "            \"description\": \"\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000001\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"2019-07-24 16:30:35\",\n"
						+ "            \"unit_design\": \"0003_0000000006\"\n" + "        },\n" + "        {\n"
						+ "            \"biz_key\": \"\",\n" + "            \"code\": \"DB\",\n"
						+ "            \"orchestration\": 231,\n" + "            \"package\": \"0011_0000000010\",\n"
						+ "            \"r_guid\": \"0008_0000000007\",\n" + "            \"description\": \"aa\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000007\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"\",\n" + "            \"unit_design\": \"0003_0000000007\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"code\": \"APP\",\n" + "            \"orchestration\": null,\n"
						+ "            \"package\": \"\",\n" + "            \"r_guid\": \"0008_0000000001\",\n"
						+ "            \"description\": \"\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000003\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"2019-07-24 16:30:35\",\n"
						+ "            \"unit_design\": \"0003_0000000006\"\n" + "        },\n" + "        {\n"
						+ "            \"biz_key\": \"\",\n" + "            \"code\": \"DB\",\n"
						+ "            \"orchestration\": 231,\n" + "            \"package\": \"0011_0000000010\",\n"
						+ "            \"r_guid\": \"0008_0000000007\",\n" + "            \"description\": \"aa\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000004\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"\",\n" + "            \"unit_design\": \"0003_0000000007\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/running_instance/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"charge_type\": 115,\n" + "            \"code\": \"APP_01\",\n"
						+ "            \"orchestration\": null,\n" + "            \"r_guid\": \"0015_0000000001\",\n"
						+ "            \"instance_mem\": 2,\n" + "            \"description\": \"APP_01\",\n"
						+ "            \"type\": 174,\n" + "            \"key_name\": \"ECIF-CORE_PRD-APP_APP_01\",\n"
						+ "            \"instance_num\": 3,\n" + "            \"unit\": \"0008_0000000001\",\n"
						+ "            \"asset_code\": \"\",\n" + "            \"port\": \"\",\n"
						+ "            \"instance_disk\": 100,\n" + "            \"host\": \"0012_0000000003\",\n"
						+ "            \"id\": \"0015_0000000001\",\n" + "            \"state\": 40,\n"
						+ "            \"fixed_date\": \"\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));
	}
	
	public void mockPackageNameWithDashAndFwdNodeExpressionServer(MockRestServiceServer server) {
		// mockFwdNodeExpression
		server.expect(ExpectedCount.once(),
				requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"key_name\": \"DEMO1\",\n"
						+ "            \"p_guid\": null,\n" + "            \"business_group\": \"business_group_A\",\n"
						+ "            \"code\": \"DEMO1\",\n" + "            \"r_guid\": \"0001_0000000001\",\n"
						+ "            \"name\": \"演示系统1\",\n" + "            \"description\": \"1\",\n"
						+ "            \"id\": \"0001_0000000001\",\n" + "            \"state\": \"new\",\n"
						+ "            \"fixed_date\": \"\"\n" + "        }\n]}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.once(),
				requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"code\": \"APP\",\n"
						+ "            \"deploy_package\": [],\n" + "            \"manager\": \"\",\n"
						+ "            \"r_guid\": \"0009_0000000008\",\n"
						+ "            \"security_group_asset_code\": \"\",\n"
						+ "            \"monitor_port\": \"20008\",\n" + "            \"description\": \"\",\n"
						+ "            \"resource_set\": [\n" + "                \"0022_0000000001\",\n"
						+ "                \"0022_0000000027\"\n" + "            ],\n"
						+ "            \"unit_type\": [\n" + "                \"tomcat8\"\n" + "            ],\n"
						+ "            \"key_name\": \"DEMO1_PRD_ADM_APP\",\n"
						+ "            \"subsys\": \"0008_0000000007\",\n" + "            \"p_guid\": null,\n"
						+ "            \"port\": \"22099\",\n" + "            \"id\": \"0009_0000000008\",\n"
						+ "            \"state\": \"created\",\n" + "            \"fixed_date\": \"\",\n"
						+ "            \"unit_design\": \"0003_0000000016\"\n" + "        },\n" + "        {\n"
						+ "            \"code\": \"APP\",\n" + "            \"deploy_package\": [],\n"
						+ "            \"manager\": \"\",\n" + "            \"r_guid\": \"0009_0000000009\",\n"
						+ "            \"security_group_asset_code\": \"\",\n"
						+ "            \"monitor_port\": \"20008\",\n" + "            \"description\": \"\",\n"
						+ "            \"resource_set\": [\n" + "                \"0022_0000000019\"\n"
						+ "            ],\n" + "            \"unit_type\": [\n" + "                \"tomcat8\"\n"
						+ "            ],\n" + "            \"key_name\": \"WECUBE_PRD_CORE_APP\",\n"
						+ "            \"subsys\": \"0008_0000000009\",\n" + "            \"p_guid\": null,\n"
						+ "            \"port\": \"8080\",\n" + "            \"id\": \"0009_0000000009\",\n"
						+ "            \"state\": \"created\",\n" + "            \"fixed_date\": \"\",\n"
						+ "            \"unit_design\": \"0003_0000000020\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));
	}

	public void mockOneLinkWithOpToOnlyExpressionServer(MockRestServiceServer server) {
		// mockOneLinkWithOpToOnlyExpression
		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"business_group\": 105,\n" + "            \"code\": \"ADMBATCH\",\n"
						+ "            \"orchestration\": null,\n" + "            \"r_guid\": \"0002_0000000006\",\n"
						+ "            \"description\": \"ADM Batch Subsystem\",\n"
						+ "            \"dcn_design_type\": 132,\n" + "            \"key_name\": \"EDP-ADMBATCH\",\n"
						+ "            \"name\": \"ADM Batch Subsystem\",\n"
						+ "            \"id\": \"0002_0000000006\",\n" + "            \"state\": 34,\n"
						+ "            \"fixed_date\": \"2019-07-24 16:28:25\",\n"
						+ "            \"system_design\": \"0001_0000000001\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"key_name\": \"EDP\",\n" + "            \"business_group\": 105,\n"
						+ "            \"code\": \"EDP\",\n" + "            \"orchestration\": null,\n"
						+ "            \"r_guid\": \"0001_0000000001\",\n"
						+ "            \"name\": \"Deposit Micro Core System\",\n"
						+ "            \"description\": \"Deposit Micro Core System\",\n"
						+ "            \"id\": \"0001_0000000001\",\n" + "            \"state\": 34,\n"
						+ "            \"fixed_date\": \"2019-07-24 17:28:15\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));
	}

	public void mockOneLinkWithOpByOnlyExpressionServer(MockRestServiceServer server) {
		// mockOneLinkWithOpByOnlyExpression

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"subsys_design\": \"0002_0000000010\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD\",\n" + "            \"code\": \"CORE\",\n"
						+ "            \"orchestration\": null,\n" + "            \"manager\": \"nertonsong\",\n"
						+ "            \"r_guid\": \"0007_0000000001\",\n"
						+ "            \"description\": \"ECIF-CORE PRD\",\n"
						+ "            \"id\": \"0007_0000000001\",\n" + "            \"state\": 37,\n"
						+ "            \"env\": 111,\n" + "            \"fixed_date\": \"2019-07-24 16:30:17\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/unit/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"code\": \"APP\",\n" + "            \"orchestration\": null,\n"
						+ "            \"package\": \"\",\n" + "            \"r_guid\": \"0008_0000000001\",\n"
						+ "            \"description\": \"\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-APP\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000001\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"2019-07-24 16:30:35\",\n"
						+ "            \"unit_design\": \"0003_0000000006\"\n" + "        },\n" + "        {\n"
						+ "            \"biz_key\": \"\",\n" + "            \"code\": \"DB\",\n"
						+ "            \"orchestration\": 231,\n" + "            \"package\": \"0011_0000000010\",\n"
						+ "            \"r_guid\": \"0008_0000000007\",\n" + "            \"description\": \"aa\",\n"
						+ "            \"resource_set\": \"0020_0000000001\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD-DB\",\n" + "            \"instance_num\": 1,\n"
						+ "            \"subsys\": \"0007_0000000001\",\n"
						+ "            \"id\": \"0008_0000000007\",\n" + "            \"state\": 37,\n"
						+ "            \"fixed_date\": \"\",\n" + "            \"unit_design\": \"0003_0000000007\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

	}

	public void mockMultipleLinksWithOpToOnlyExpressionServer(MockRestServiceServer server) {
		// first expression
		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/subsys/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"subsys_design\": \"0002_0000000010\",\n"
						+ "            \"key_name\": \"ECIF-CORE_PRD\",\n" + "            \"code\": \"CORE\",\n"
						+ "            \"orchestration\": null,\n" + "            \"manager\": \"nertonsong\",\n"
						+ "            \"r_guid\": \"0007_0000000001\",\n"
						+ "            \"description\": \"ECIF-CORE PRD\",\n"
						+ "            \"id\": \"0007_0000000001\",\n" + "            \"state\": 37,\n"
						+ "            \"env\": 111,\n" + "            \"fixed_date\": \"2019-07-24 16:30:17\"\n"
						+ "        }\n" + "    ]\n" + "}", MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/subsys_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"business_group\": 105,\n" + "            \"code\": \"CORE\",\n"
						+ "            \"orchestration\": null,\n" + "            \"r_guid\": \"0002_0000000010\",\n"
						+ "            \"description\": \"CRM Core Subsystem\",\n"
						+ "            \"dcn_design_type\": 135,\n" + "            \"key_name\": \"ECIF-CORE\",\n"
						+ "            \"name\": \"CRM Core Subsystem\",\n"
						+ "            \"id\": \"0002_0000000010\",\n" + "            \"state\": 34,\n"
						+ "            \"fixed_date\": \"2019-07-24 16:28:27\",\n"
						+ "            \"system_design\": \"0001_0000000003\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));

		server.expect(ExpectedCount.manyTimes(),
				requestTo(String.format("http://%s/wecmdb/entities/system_design/query", this.gatewayUrl)))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withSuccess("{\n" + "    \"status\": \"OK\",\n" + "    \"message\": \"Success\",\n"
						+ "    \"data\": [\n" + "        {\n" + "            \"biz_key\": null,\n"
						+ "            \"key_name\": \"ECIF\",\n" + "            \"business_group\": 105,\n"
						+ "            \"code\": \"ECIF\",\n" + "            \"orchestration\": null,\n"
						+ "            \"r_guid\": \"0001_0000000003\",\n" + "            \"name\": \"CRM System\",\n"
						+ "            \"description\": \"CRM System\",\n"
						+ "            \"id\": \"0001_0000000003\",\n" + "            \"state\": 34,\n"
						+ "            \"fixed_date\": \"2019-07-24 17:28:17\"\n" + "        }\n" + "    ]\n" + "}",
						MediaType.APPLICATION_JSON));
	}
}
