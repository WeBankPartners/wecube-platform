package com.webank.wecube.core.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

	@Test
	public void containsOnlyAlphanumericOrHyphen() {
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen(null)).isFalse();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("")).isFalse();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen(" ")).isFalse();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("abc")).isTrue();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("123")).isTrue();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("-")).isTrue();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("Abc-123")).isTrue();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("abc/123")).isFalse();
		assertThat(StringUtils.containsOnlyAlphanumericOrHyphen("中文")).isFalse();
	}
}