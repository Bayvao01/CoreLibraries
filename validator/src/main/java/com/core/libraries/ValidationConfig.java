package com.core.libraries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationConfig {
	private Map<String, List<String>> fieldMap = new HashMap<>();
	private Map<String, List<String>> fieldValidationMap = new HashMap<>();
	private Map<String, Map<String, String>> fieldValidationValueMap;
	private boolean htmlCheck = false;

	public Map<String, List<String>> getFieldMap() {
		return fieldMap;
	}

	public Map<String, List<String>> getFieldValidationMap() {
		return fieldValidationMap;
	}

	public Map<String, Map<String, String>> getFieldValidationValueMap() {
		if (fieldValidationValueMap == null) {
			init();
		}
		return fieldValidationValueMap;
	}

	public boolean isHtmlCheck() {
		return htmlCheck;
	}

	public void setHtmlCheck(boolean htmlCheck) {
		this.htmlCheck = htmlCheck;
	}

	private synchronized void init() {
		Map<String, String> fieldValParameterMap;
		Map<String, Map<String, String>> fieldValidationRuleMap = new HashMap<>();

		for (Map.Entry<String, List<String>> entrySet : fieldValidationMap.entrySet()) {
			List<String> fieldValidationList = entrySet.getValue();
			if (fieldValidationList == null || fieldValidationList.isEmpty())
				continue;

			fieldValParameterMap = new HashMap<>();
			String previousValName = null;
			String previousValParameter = null;
			for (String fieldValidation : fieldValidationList) {
				fieldValidation = fieldValidation.trim();
				if (!Character.isUpperCase(fieldValidation.charAt(0))) {
					StringBuilder sb = new StringBuilder();
					sb.append(previousValParameter).append(",").append(fieldValidation);
					previousValParameter = sb.toString();
					fieldValParameterMap.put(previousValName, previousValParameter);
				} else {
					String valName = fieldValidation.substring(0, fieldValidation.indexOf(':'));
					String valParameter = fieldValidation.substring(fieldValidation.indexOf(':') + 1,
							fieldValidation.length());

					previousValName = valName.trim().toUpperCase();
					previousValParameter = valParameter.trim();
					fieldValParameterMap.put(previousValName, previousValParameter);
				}
			}
			fieldValidationRuleMap.put(entrySet.getKey(), fieldValParameterMap);
		}
		fieldValidationValueMap = fieldValidationRuleMap;
	}

}
