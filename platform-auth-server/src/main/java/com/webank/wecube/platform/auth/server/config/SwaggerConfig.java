package com.webank.wecube.platform.auth.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * @author gavin
 *
 */
@Profile({ ApplicationConstants.Profile.DEV, ApplicationConstants.Profile.TEST, ApplicationConstants.Profile.DEFAULT,
		ApplicationConstants.Profile.MOCK })
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {
	public static final String SWAGGER_BASE_PACKAGE = ApplicationConstants.ApiInfo.BASE_PACKAGE;
	public static final String SWAGGER_VERSION = ApplicationConstants.ApiInfo.VERSION_V1;
	public static final String SWAGGER_TITLE = ApplicationConstants.ApiInfo.TITLE;

	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
				.apis(RequestHandlerSelectors.basePackage(SWAGGER_BASE_PACKAGE)).paths(PathSelectors.any()).build();
	}

	@Bean
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder().title(SWAGGER_TITLE).version(SWAGGER_VERSION).build();
	}

	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
	}
}
