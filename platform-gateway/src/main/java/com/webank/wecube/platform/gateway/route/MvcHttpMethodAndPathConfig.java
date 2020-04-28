package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MvcHttpMethodAndPathConfig {

	private final MvcHttpMethodAndPath mvcHttpMethodAndPath;

	private long createdTime;
	private long lastModifiedTime;
	private boolean disabled;

	private long version;

	private List<HttpDestination> httpDestinations = new ArrayList<>();

	public MvcHttpMethodAndPathConfig(MvcHttpMethodAndPath mvcHttpMethodAndPath) {
		super();
		this.mvcHttpMethodAndPath = mvcHttpMethodAndPath;
		this.createdTime = System.currentTimeMillis();
		this.lastModifiedTime = System.currentTimeMillis();
	}

	boolean tryAddHttpDestination(HttpDestination httpDestination) {
		if (httpDestination == null) {
			return false;
		}

		HttpDestination exist = findHttpDestination(httpDestination);
		if (exist == null) {
			HttpDestination newHttpDestination = new HttpDestination(httpDestination.getScheme(),
					httpDestination.getHost(), httpDestination.getPort(), httpDestination.getWeight());
			newHttpDestination.version(this.version);

			this.httpDestinations.add(newHttpDestination);

			return true;
		}

		exist.version(version);
		exist.weight(httpDestination.getWeight());
		return true;
	}

	private HttpDestination findHttpDestination(HttpDestination criteria) {
		if (criteria == null) {
			return null;
		}

		for (HttpDestination h : this.httpDestinations) {
			if (h.equals(criteria)) {
				return h;
			}
		}

		return null;
	}

	MvcHttpMethodAndPathConfig clearHttpDestinations() {
		this.httpDestinations.clear();
		return this;
	}

	MvcHttpMethodAndPathConfig cleanOutdatedHttpDestination() {
		List<HttpDestination> toRemoves = this.httpDestinations //
				.stream() //
				.filter(c -> c.getVersion() < version) //
				.collect(Collectors.toList()); //

		this.httpDestinations.removeAll(toRemoves);

		return this;
	}

	MvcHttpMethodAndPathConfig version(long version) {
		this.version = version;
		lastModifiedTime();
		return this;
	}

	MvcHttpMethodAndPathConfig disable() {
		this.disabled = true;
		lastModifiedTime();
		return this;
	}

	MvcHttpMethodAndPathConfig enable() {
		this.disabled = false;
		lastModifiedTime();
		return this;
	}

	public MvcHttpMethodAndPath getMvcHttpMethodAndPath() {
		return mvcHttpMethodAndPath;
	}

	public List<HttpDestination> getHttpDestinations() {
		return Collections.unmodifiableList(httpDestinations);
	}

	private MvcHttpMethodAndPathConfig lastModifiedTime() {
		this.lastModifiedTime = System.currentTimeMillis();
		return this;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public long getLastModifiedTime() {
		return lastModifiedTime;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[mvcHttpMethodAndPath=");
		builder.append(mvcHttpMethodAndPath);
		builder.append(", createdTime=");
		builder.append(createdTime);
		builder.append(", lastModifiedTime=");
		builder.append(lastModifiedTime);
		builder.append(", disabled=");
		builder.append(disabled);
		builder.append(", version=");
		builder.append(version);
		builder.append(", httpDestinations=");
		builder.append(httpDestinations);
		builder.append("]");
		return builder.toString();
	}

}
