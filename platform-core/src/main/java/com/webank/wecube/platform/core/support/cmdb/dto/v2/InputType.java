package com.webank.wecube.platform.core.support.cmdb.dto.v2;

public enum InputType {
	NONE("none"),
	TEXT("text"),
	DATE("date"),
	TEXT_AREA("textArea"),
	DROPLIST("select"),
	MULT_SEL_DROPLIST("multiSelect"),
	REFERENCE("ref"),
	MULT_REF("multiRef"),
	NUMBER("number"),
	ORCHESTRATION_MULI_REF("orchestration_multi_ref"),
	ORCHESTRATION("orchestration_ref");

	private String code;

	private InputType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	static public InputType fromCode(String code) {
		for(InputType inputType:values()) {
			if(NONE.equals(inputType))
				continue;

			if(inputType.getCode().equals(code)) {
				return inputType;
			}
		}
		return NONE;
	}
}
