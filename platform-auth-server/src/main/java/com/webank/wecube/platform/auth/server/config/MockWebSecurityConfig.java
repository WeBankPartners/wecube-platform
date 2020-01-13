package com.webank.wecube.platform.auth.server.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.webank.wecube.platform.auth.server.common.ApplicationConstants;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedAuthenticationFilter;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedLoginFilter;
import com.webank.wecube.platform.auth.server.http.filter.JwtSsoBasedSecurityContextRepository;

@Profile({ ApplicationConstants.Profile.MOCK })
@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = false)
public class MockWebSecurityConfig extends AuthSecurityConfigurerAdapter {

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		warnNotLoadingProdSecurityConfigurationNotice();
	}

	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		log.info("=== configure AuthenticationManagerBuilder ===");
		auth.inMemoryAuthentication() //
				.withUser("marissa") //
				.password("{noop}koala") //
				.roles("USER") //
				.and() //
				.withUser("paul") //
				.password("{noop}emu") //
				.roles("USER") //
				.and() //
				.withUser("umadmin") //
				.password("{noop}123456") //
				.roles("TESTER") //
		; //
	}

	protected void configure(HttpSecurity http) throws Exception {
		http //
				.cors() //
				.and() //
				.csrf() //
				.disable() //
				.sessionManagement() //
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //
				.and() //
				.securityContext() //
				.securityContextRepository(new JwtSsoBasedSecurityContextRepository()) //
				.and() //
				.addFilterBefore(new JwtSsoBasedLoginFilter(authenticationManager(),authServerProperties),
						SecurityContextPersistenceFilter.class) //
				.addFilter(new JwtSsoBasedAuthenticationFilter(authenticationManager(), authServerProperties))//
				.authorizeRequests() //
				.antMatchers("/**") //
				.permitAll() //
		; //

	}
}
