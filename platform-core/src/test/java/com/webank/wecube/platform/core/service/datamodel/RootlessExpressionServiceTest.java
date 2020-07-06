package com.webank.wecube.platform.core.service.datamodel;

import com.webank.wecube.platform.core.BaseSpringBootTest;
import com.webank.wecube.platform.core.commons.ApplicationProperties;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.DmeFilterDto;
import com.webank.wecube.platform.core.dto.DmeLinkFilterDto;
import com.webank.wecube.platform.core.dto.Filter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class RootlessExpressionServiceTest extends BaseSpringBootTest {

	@Autowired
	RootlessExpressionServiceImpl rootlessExpressionService;
	@Autowired
	@Qualifier(value = "jwtSsoRestTemplate")
	private RestTemplate jwtSsoRestTemplate;
	@Autowired
	private ApplicationProperties applicationProperties;
	private String gatewayUrl;
	private MockRestServiceServer server;

	@Before
	public void setup() {
		server = MockRestServiceServer.bindTo(jwtSsoRestTemplate).build();
		gatewayUrl = this.applicationProperties.getGatewayUrl();
	}

	public void testParseFilters() {
//        String expr = ""
	}

	

	@Test
	public void givenExpressionWithWrongNameFilterShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockPackageNameWithDashAndFwdNodeExpressionServer(server);
		try {
			List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(
					new DmeFilterDto("wecmdb:system_design.code", Collections.singletonList(new DmeLinkFilterDto(0,
							"platform", "core", Collections.singletonList(new Filter("key_name", "eq", "DEMO1"))))));
			assertThat(resultOne.size()).isEqualTo(1);
			assertThat(resultOne.get(0)).isEqualTo("DEMO1");
		} catch (WecubeCoreException e) {
			assertThat(e.getMessage()).contains("don't match to the name in DME");
		}

		try {
			List<Object> resultTwo = rootlessExpressionService.fetchDataWithFilter(
					new DmeFilterDto("wecmdb:unit.key_name", Collections.singletonList(new DmeLinkFilterDto(0,
							"platform", "unit", Collections.singletonList(new Filter("code", "eq", "APP"))))));
		} catch (WecubeCoreException e) {
			assertThat(e.getMessage()).contains("don't match to the name in DME");
		}
		server.verify();
	}

	@Test
	public void givenExpressionWithEmptyFilterThenShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockPackageNameWithDashAndFwdNodeExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(
				new DmeFilterDto("wecmdb:system_design.code", Collections.singletonList(new DmeLinkFilterDto())));
		assert resultOne.get(0).equals("DEMO1");

		List<Object> resultTwo = rootlessExpressionService.fetchDataWithFilter(
				new DmeFilterDto("wecmdb:unit.key_name", Collections.singletonList(new DmeLinkFilterDto())));
		assert resultTwo.get(0).equals("DEMO1_PRD_ADM_APP");
		assert resultTwo.get(1).equals("WECUBE_PRD_CORE_APP");

		server.verify();
	}

	@Test
	public void givenExpressionWithFilterShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockPackageNameWithDashAndFwdNodeExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(
				new DmeFilterDto("wecmdb:system_design.code", Collections.singletonList(new DmeLinkFilterDto(0,
						"wecmdb", "system_design", Collections.singletonList(new Filter("key_name", "eq", "DEMO1"))))));
		assertThat(resultOne.size()).isEqualTo(1);
		assertThat(resultOne.get(0)).isEqualTo("DEMO1");

		List<Object> resultTwo = rootlessExpressionService.fetchDataWithFilter(
				new DmeFilterDto("wecmdb:unit.key_name", Collections.singletonList(new DmeLinkFilterDto(0, "wecmdb",
						"unit", Collections.singletonList(new Filter("code", "eq", "APP"))))));
		assertThat(resultTwo.size()).isEqualTo(2);
		assertThat(resultTwo.get(0)).isEqualTo("DEMO1_PRD_ADM_APP");
		assertThat(resultTwo.get(1)).isEqualTo("WECUBE_PRD_CORE_APP");

		server.verify();
	}

	

	@Test
	public void wecmdbOneLinkWithOpToExpressionAndCorrectFiltersShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockOneLinkWithOpToOnlyExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService
				.fetchDataWithFilter(new DmeFilterDto("wecmdb:subsys_design.system_design>wecmdb:system_design.code",
						Arrays.asList(
								new DmeLinkFilterDto(0, "wecmdb", "subsys_design",
										Collections.singletonList(new Filter("business_group", "eq", 105))),
								new DmeLinkFilterDto(1, "wecmdb", "system_design",
										Collections.singletonList(new Filter("state", "eq", 34))))));
		assert resultOne.get(0).equals("EDP");

		server.verify();
	}

	@Test
	public void wecmdbOneLinkWithOpByExpressionFetchShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockOneLinkWithOpByOnlyExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService
				.fetchDataWithFilter(new DmeFilterDto("wecmdb:subsys~(subsys)wecmdb:unit.fixed_date",
						Arrays.asList(
								new DmeLinkFilterDto(0, "wecmdb", "subsys",
										Collections.singletonList(new Filter("code", "eq", "CORE"))),
								new DmeLinkFilterDto(1, "wecmdb", "unit",
										Collections.singletonList(new Filter("subsys", "eq", "0007_0000000001"))))));
		assertThat(resultOne.size()).isEqualTo(2);
		assertThat(resultOne).containsExactlyInAnyOrder("2019-07-24 16:30:35", "");

		server.verify();
	}

	@Test
	public void wecmdbMultipleLinksWithOpToOnlyExpressionFetchShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockMultipleLinksWithOpToOnlyExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto(
				"wecmdb:subsys.subsys_design>wecmdb:subsys_design.system_design>wecmdb:system_design.key_name",
				Collections.singletonList(new DmeLinkFilterDto(2, "wecmdb", "system_design",
						Collections.singletonList(new Filter("name", "eq", "CRM System"))))));
		assertThat(resultOne.size()).isEqualTo(1);
		assertThat(resultOne).containsExactlyInAnyOrder("ECIF");

		server.verify();
	}

	@Test
	public void wecmdbMultipleLinksWithOpByOnlyExpressionFetchShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockMultipleLinksWithOpByOnlyExpressionServer(server);

		List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(
				new DmeFilterDto("wecmdb:subsys~(subsys)wecmdb:unit~(unit)wecmdb:running_instance.id",
						Collections.singletonList(new DmeLinkFilterDto(1, "wecmdb", "unit",
								Collections.singletonList(new Filter("id", "eq", "0008_0000000001"))))));
		assertThat(resultOne.size()).isEqualTo(1);
		assertThat(resultOne).containsExactlyInAnyOrder("0015_0000000001");

		server.verify();
	}

	@Test
	public void wecmdbMultipleLinksWithMixedOpExpressionFetchShouldSucceed() {
		new RootlessExpressionServiceMocker(this.gatewayUrl).mockMultipleLinksWithMixedOpExpressionServer(server);
		List<Object> resultOne = rootlessExpressionService.fetchDataWithFilter(new DmeFilterDto(
				"wecmdb:subsys~(subsys)wecmdb:unit.unit_design>wecmdb:unit_design.subsys_design>wecmdb:subsys_design.key_name",
				Collections.singletonList(new DmeLinkFilterDto(1, "wecmdb", "unit", Arrays
						.asList(new Filter("state", "eq", 37), new Filter("unit_design", "eq", "0003_0000000006"))))));

		assertThat(resultOne.size()).isEqualTo(1);
		assertThat(resultOne).containsExactlyInAnyOrder("ECIF-CORE");

		server.verify();
	}

	

	
}
