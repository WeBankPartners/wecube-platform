package com.webank.wecube.core.controller.helper;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.springframework.http.HttpMethod.valueOf;
import static org.springframework.http.RequestEntity.method;

public class ProxyExchange {
	private static Set<String> DEFAULT_SENSITIVE = new HashSet<>(Arrays.asList("cookie", "authorization"));

	private RestTemplate rest;

	private HttpServletRequest request;
	private HttpServletResponse response;
	private Object body;
	private Set<String> sensitiveHeaders;
	private HttpHeaders httpHeaders;
	private URI proxyUri;
	private URI targetUri;

	public ProxyExchange(RestTemplate rest, HttpServletRequest request, HttpServletResponse response) {
		this.rest = rest;
		this.request = request;
		this.response = response;

		this.proxyUri = toURI(request.getRequestURL().toString());
		this.httpHeaders = readRequestHeaders(request);
	}

	public ProxyExchange body(Object body) {
		this.body = body;
		return this;
	}

	public ProxyExchange customHttpHeaders(Map<String, String> headerMap) {
		for (Map.Entry<String, String> header : headerMap.entrySet()) {
			httpHeaders.set(header.getKey(), header.getValue());
		}
		return this;
	}

	public ProxyExchange header(String name, String... value) {
		this.httpHeaders.put(name, Arrays.asList(value));
		return this;
	}

	public ProxyExchange sensitiveHeaders(Set<String> sensitiveHeaders) {
		this.sensitiveHeaders = sensitiveHeaders;
		return this;
	}

	public ProxyExchange targetUri(String uri) {
		this.targetUri = toURI(uri);
		return this;
	}

	public String path() {
		return this.request.getServletPath();
	}

	public String path(String prefix) {
		String path = this.path();
		if (!path.startsWith(prefix)) {
			throw new IllegalArgumentException("Path does not start with prefix (" + prefix + "): " + path);
		} else {
			return path.substring(prefix.length());
		}
	}

	public void exchange() {
		BodyBuilder httpBodyBuilder = method(valueOf(request.getMethod()), this.targetUri);
		applyHeaders(httpBodyBuilder);
		RequestEntity<?> requestEntity = this.body == null ? httpBodyBuilder.build() : httpBodyBuilder.body(this.body);
		ResponseEntity<byte[]> responseEntity = this.rest.exchange(requestEntity, ParameterizedTypeReference.forType(byte[].class));
		new HttpServletResponseWriter(response).writeHttpResponse(responseEntity);
	}

	private void applyHeaders(BodyBuilder builder) {
		Set<String> sensitive = this.sensitiveHeaders;
		if (sensitive == null) {
			sensitive = DEFAULT_SENSITIVE;
		}
		this.appendForwarded(proxyUri);
		this.appendXForwarded(proxyUri);

		for (String name : this.httpHeaders.keySet()) {
			if (!sensitive.contains(name.toLowerCase())) {
				builder.header(name, (String[]) this.httpHeaders.get(name).toArray(new String[0]));
			}
		}
	}

	private HttpHeaders readRequestHeaders(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName, Collections.list(request.getHeaders(headerName)));
		}
		return headers;
	}

	private void appendXForwarded(URI uri) {
		boolean success = appendHeader("x-forwarded-host", uri.getHost(), true);
		if (success) {
			appendHeader("x-forwarded-proto", uri.getScheme(), true);
		}
	}

	private void appendForwarded(URI uri) {
		appendHeader("forwarded", this.forwarded(uri), false);
	}

	private boolean appendHeader(String headerName, String appendHeaderValue, boolean skipIfAbsent) {
		String headerValue = this.httpHeaders.getFirst(headerName);
		if (headerValue != null) {
			this.httpHeaders.set(headerName, headerValue + "," + appendHeaderValue);
			return true;
		} else {
			if (skipIfAbsent) {
				return false;
			} else {
				this.httpHeaders.set(headerName, appendHeaderValue);
				return true;
			}
		}
	}

	private String forwarded(URI uri) {
		return "http".equals(uri.getScheme()) ?  "host=" + uri.getHost() : String.format("host=%s;proto=%s", uri.getHost(), uri.getScheme());
	}

	private URI toURI(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Cannot create URI : " + uri, e);
		}
	}
}