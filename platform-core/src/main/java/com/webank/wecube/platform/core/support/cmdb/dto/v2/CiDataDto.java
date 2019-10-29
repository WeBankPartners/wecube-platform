package com.webank.wecube.platform.core.support.cmdb.dto.v2;

import java.util.HashMap;

import org.apache.commons.beanutils.BeanMap;

@SuppressWarnings({"rawtypes","unchecked"})
public class CiDataDto extends HashMap {
	private static final long serialVersionUID = 1L;

	public CiDataDto(Object ciObj) {
		super(new BeanMap (ciObj));
	}
}