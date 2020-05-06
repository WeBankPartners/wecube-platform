package com.webank.wecube.platform.gateway.filter.factory;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.gateway.dto.CommonResponseDto;
import com.webank.wecube.platform.gateway.parser.DefaultJwtSsoTokenParser;
import com.webank.wecube.platform.gateway.parser.JwtSsoTokenParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;

public class JwtSsoTokenGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtSsoTokenGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtSsoTokenGatewayFilterFactory.class);

    public static final String AUTHENTICATED_KEY = "authenticated";

    public static final String TOKEN_PREFIX = "Bearer";

    private String headerValue = "Bearer realm=\"Central Authentication Server\";profile=\"JWT\";";

    private ObjectMapper objectMapper = new ObjectMapper();

    private JwtSsoTokenParser jwtParser = new DefaultJwtSsoTokenParser();

    public JwtSsoTokenGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        if (log.isDebugEnabled()) {
            log.debug("Filter-{} applied", JwtSsoTokenGatewayFilterFactory.class.getSimpleName());
        }
        
        return ((exchange, chain) -> {
            ServerHttpRequest req = exchange.getRequest();
            log.debug("Filter-{},uri:{}", JwtSsoTokenGatewayFilterFactory.class.getSimpleName(), req.getURI().toString());
            
            boolean authenticated = config.isAuthenticated();
            if (!authenticated) {
                return chain.filter(exchange);
            }

            ServerHttpRequest request = exchange.getRequest();
            List<String> authorizationHeaders = request.getHeaders().get("Authorization");

            String token = null;
            if (authorizationHeaders != null) {
                for (String authorizationHeader : authorizationHeaders) {
                    if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
                        token = authorizationHeader
                                .substring(authorizationHeader.indexOf(TOKEN_PREFIX) + TOKEN_PREFIX.length() + 1);
                    }
                }
            }

            if (token == null || StringUtils.isBlank(token)) {
                return handleAuthenticationFailure(exchange, "Access token does not provide.");
            }

            Jws<Claims> jwt = null;
            try {
                jwt = jwtParser.parseJwt(token);
            } catch (ExpiredJwtException e) {
                return handleAuthenticationFailure(exchange, "Access token has expired.");
            } catch (JwtException e) {
                return handleAuthenticationFailure(exchange, "Access token is not available.");
            }

            if (jwt == null) {
                return handleAuthenticationFailure(exchange, "Cannot process JWT token.");
            }

            return chain.filter(exchange);
        });
    }

    protected Mono<Void> handleAuthenticationFailure(ServerWebExchange exchange, String errorMsg) {
        CommonResponseDto responseDto = CommonResponseDto.error(errorMsg);
        ServerHttpResponse response = exchange.getResponse();
        try {
            byte[] bits = objectMapper.writeValueAsBytes(responseDto);
            DataBuffer buffer = response.bufferFactory().wrap(bits);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            response.getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, headerValue);

            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.debug("failed to process json", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(AUTHENTICATED_KEY);
    }

    public static class Config {

        private boolean authenticated;

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

    }

}
