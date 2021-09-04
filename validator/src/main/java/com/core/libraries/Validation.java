package com.core.libraries;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.routines.DateValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Validator implementation that provides support for validating any
 * object's simple, collection and object array properties for defined
 * validation rules such as: REQUIRED, EMPTY, LEN, MINLEN, MAXLEN, PATTERN, and
 * DATEPATTERN.
 * 
 */
public class Validation {

	private static final Logger LOG = LoggerFactory.getLogger(Validation.class);
	private static final String LOG_ISVALID = "property: {}, validation: {}:{}, value: {}, isValid?: {}";
	private static final Map<String, String> MESSAGES = new HashMap<>();

	static {
		MESSAGES.put("EMPTY", "Value should not be empty");
		MESSAGES.put("LEN", "Length should be ");
		MESSAGES.put("MINLEN", "Min length should be ");
		MESSAGES.put("MAXLEN", "Max length should be ");
		MESSAGES.put("PATTERN", "Value should be in format ");
		MESSAGES.put("DATEPATTERN", "Date should be in format ");
		MESSAGES.put("EMAIL", "Email Id format is invalid");
		MESSAGES.put("HTML", "HTML characters not allowed in value.");
		MESSAGES.put("", "");
	}

	private boolean throwOnValidationFailure = true;
	private Map<String, List<String>> screenFieldMap;
	private Map<String, Map<String, String>> fieldValidationValueMap;
	private boolean htmlCheck = false;

	/**
	 * Initialize this Validator with input configuration and
	 * <code>throwOnValidationFailure</code> is set to true
	 *
	 * @param config ValidatorConfig object to be used during validate call.
	 */

	public Validation(ValidationConfig config) {
		this(config, true);

	}

	/**
	 * Initialize this Validator with input configuration. The
	 * <code>throwOnValidationFailure</code> should be set to true if validate call
	 * is expected to throw ValidationException on any field validation failure.
	 *
	 * @param config                   ValidatorConfig object to be used during
	 *                                 validate call.
	 * @param throwOnValidationFailure true if ValidationException should be raised,
	 *                                 false otherwise.
	 */

	public Validation(ValidationConfig config, boolean throwOnValidationFailure) {

		if (config == null) {
			throw new IllegalArgumentException("Config can't be null");
		}
		screenFieldMap = config.getFieldMap();
		fieldValidationValueMap = config.getFieldValidationValueMap();
		this.throwOnValidationFailure = throwOnValidationFailure;
		htmlCheck = config.isHtmlCheck();
	}

	/**
	 * 
	 * Validates fields of input object using validation rules defined against input
	 * screenName.
	 * 
	 * @param screenName screen name for which validation rules are defined.
	 * 
	 * @param object     business object to validate
	 * 
	 * @return list of FieldValidationError if <code>throwOnValidationFailure</code>
	 *         is set to false
	 * @throws ValidationException if validation of any field fails and
	 *                             <code>throwOnValidationFailure</code> is set to
	 *                             true
	 * 
	 */

	public List<FieldValidationError> validate(String screenName, Object object) {

		if (isBlank(screenName)) {
			throw new IllegalArgumentException("screenName can't be null or empty");
		}

		if (object == null) {
			throw new IllegalArgumentException("object can't be null");
		}

		List<FieldValidationError> errors = new ArrayList<>();
		List<String> screenFieldList = screenFieldMap.get(screenName);

		if (screenFieldList == null) {
			LOG.debug("No Fields to validate for screenName: {}", screenName);
			return errors;
		}

		for (String screenFieldName : screenFieldList) {
			Map<String, String> validationParamMap = fieldValidationValueMap.get(screenFieldName);

			if (validationParamMap == null || validationParamMap.isEmpty()) {
				LOG.debug("No validations found for screenFieldName: {}", screenFieldName);
				continue;
			}

			String[] nestedProperty = screenFieldName.split("[.]");
			validateProperty(object, "", nestedProperty, 0, validationParamMap, errors);
		}

		if (throwOnValidationFailure && !errors.isEmpty()) {
			LOG.info("Field validation failed with number of errors: {}", errors.size());
			throw new ValidationException(400, "Validation failed", errors);
		}

		return errors;
	}

	protected Object getPropertyValue(Object bean, String propertyName) {
		try {
			return PropertyUtils.getSimpleProperty(bean, propertyName);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | RuntimeException e) {
			LOG.error("Failed to retrieve fieldValue for fieldName: " + propertyName + " - " + e);
			throw new IllegalArgumentException(e);
		}
	}

	protected void validateProperty(Object bean, String propertyName, String[] nestedProperty, int index,
			Map<String, String> validationParamMap, List<FieldValidationError> errors) {
		if (index < nestedProperty.length) {
			/**
			 * 
			 * Add validation for REQUIRED fields if any object in nested path is null or
			 * empty
			 * 
			 */
			if (hasRequiredProperty(bean, propertyName, validationParamMap, errors)) {
				validateNestedProperty(bean, propertyName, nestedProperty, index, validationParamMap, errors);
			}
		} else {
			/**
			 * Bypass validation of OPTIONAL fields if it is null or empty
			 * 
			 */
			if (validationParamMap.containsKey("OPTIONAL") && (bean == null || !emptyCheck(bean)))
				return;
			validateSimpleProperty(bean, propertyName, validationParamMap, errors);
		}
	}

	protected boolean hasRequiredProperty(Object bean, String propertyName, Map<String, String> validationParamMap,
			List<FieldValidationError> errors) {
		if (bean == null || (bean instanceof Collection && isEmpty((Collection<?>) bean))
				|| (bean instanceof Object[] && isEmpty((Object[]) bean))) {
			if (validationParamMap.containsKey("REQUIRED")) {
				LOG.debug("property: {}, validation: {}:, value: {}, isValid?: false", propertyName, "REQUIRED", bean);
				errors.add(new FieldValidationError(propertyName, "Value is required"));
			}
			return false;
		}
		return true;
	}

	protected void validateNestedProperty(Object bean, String propertyName, String[] nestedProperty, int index,
			Map<String, String> validationParamMap, List<FieldValidationError> errors) {

		if (index == 0) {
			propertyName = nestedProperty[index];
		} else {
			propertyName = propertyName + "." + nestedProperty[index];
		}

		Object propertyValue = getPropertyValue(bean, nestedProperty[index]);
		if (propertyValue instanceof Collection) {
			hasRequiredProperty(propertyValue, propertyName, validationParamMap, errors);
			int j = 0;
			for (Object element : (Collection<?>) propertyValue) {
				String indexedProperty = propertyName + "[" + j + "]";
				validateProperty(element, indexedProperty, nestedProperty, index + 1, validationParamMap, errors);
				j++;
			}
		} else if (propertyValue instanceof Object[]) {
			hasRequiredProperty(propertyValue, propertyName, validationParamMap, errors);
			int j = 0;
			for (Object element : (Object[]) propertyValue) {
				String indexedProperty = propertyName + "[" + j + "]";
				validateProperty(element, indexedProperty, nestedProperty, index + 1, validationParamMap, errors);
				j++;
			}

		} else {
			validateProperty(propertyValue, propertyName, nestedProperty, index + 1, validationParamMap, errors);
		}
	}

	protected void validateSimpleProperty(Object propertyValue, String propertyName,
			Map<String, String> validationParamMap, List<FieldValidationError> errors) {

		FieldValidationError errorMessage;
		String paramValue = "";
		for (Map.Entry<String, String> validationParam : validationParamMap.entrySet()) {
			paramValue = validationParam.getValue();
			errorMessage = validate(propertyValue, propertyName, validationParam.getKey(), paramValue);
			if (errorMessage != null) {
				errors.add(errorMessage);
			}
		}

		if (htmlCheck && !checkHtmlChar(propertyValue)) {
			errors.add(new FieldValidationError(propertyName, MESSAGES.get("HTML")));
			LOG.debug(LOG_ISVALID, propertyName, "HTML", paramValue, propertyValue, false);
		}
	}

	protected FieldValidationError validate(Object propertyValue, String propertyName, String validationName,
			String parameter) {
		
		FieldValidationError errorMessage = null;
		boolean isValid = true;
		boolean appendParameter = true;

		switch (validationName) {
		case "EMPTY":
			isValid = emptyCheck(propertyValue);
			parameter = "";
			appendParameter = false;
			break;
		case "LEN":
			isValid = lenCheck(propertyValue, parameter);
			break;
		case "MINLEN":
			isValid = checkMinLen(propertyValue, parameter);
			break;
		case "MAXLEN":
			isValid = checkMaxLen(propertyValue, parameter);
			break;
		case "PATTERN":
			isValid = patternMatcher(propertyValue, parameter);
			break;
		case "DATEPATTERN":
			isValid = datePatternMatcher(propertyValue, parameter);
			break;
		case "EMAIL":
			isValid = checkEmail(propertyValue);
			appendParameter = false;
			break;
		default:
			break;
		}
		LOG.debug(LOG_ISVALID, propertyName, validationName, parameter, propertyValue, isValid);

		if (!isValid) {
			String validationMessage = MESSAGES.get(validationName);

			if (appendParameter)
				validationMessage += parameter;

			errorMessage = new FieldValidationError(propertyName, validationMessage);
		}
		return errorMessage;
	}

	private boolean emptyCheck(Object fieldValue) {
		if (fieldValue == null) {
			return false;
		}

		return !((fieldValue instanceof String && isBlank((String) fieldValue))
				|| (fieldValue instanceof Collection && isEmpty((Collection<?>) fieldValue)));
	}

	private boolean lenCheck(Object fieldValue, String parameter) {
		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {
			String value = (String) fieldValue;

			if (value.length() != Integer.parseInt(parameter)) {
				return false;
			}

		} else if (fieldValue instanceof Number) {
			Number value = (Number) fieldValue;

			if (String.valueOf(value).length() != Integer.parseInt(parameter)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkMinLen(Object fieldValue, String parameter) {
		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {
			String value = (String) fieldValue;

			if (value.length() < Integer.parseInt(parameter)) {
				return false;
			}

		} else if (fieldValue instanceof Number) {
			Number value = (Number) fieldValue;

			if (String.valueOf(value).length() < Integer.parseInt(parameter)) {
				return false;
			}
		}

		return true;
	}

	private boolean checkMaxLen(Object fieldValue, String parameter) {
		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {
			String value = (String) fieldValue;

			if (value.length() > Integer.parseInt(parameter)) {
				return false;
			}
		} else if (fieldValue instanceof Number) {
			Number value = (Number) fieldValue;

			if (String.valueOf(value).length() > Integer.parseInt(parameter)) {
				return false;
			}
		}
		return true;
	}

	private boolean patternMatcher(Object fieldValue, String parameter) {
		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {

			String value = (String) fieldValue;
			Pattern pattern = Pattern.compile(parameter);
			Matcher matcher = pattern.matcher(value);

			return matcher.matches();
		} else if (fieldValue instanceof Number) {
			String value = String.valueOf(fieldValue);
			Pattern pattern = Pattern.compile(parameter);
			Matcher matcher = pattern.matcher(value);
			return matcher.matches();
		}

		return true;
	}

	private boolean datePatternMatcher(Object fieldValue, String parameter) {
		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {
			String value = (String) fieldValue;
			return DateValidator.getInstance().isValid(value, parameter);
		}

		return true;

	}

	private boolean checkEmail(Object fieldValue) {

		if (fieldValue == null) {
			return false;
		}

		if (fieldValue instanceof String) {
			String value = (String) fieldValue;
			return EmailValidator.getInstance().isValid(value);
		}

		return true;

	}

	private boolean checkHtmlChar(Object fieldValue) {

		if (fieldValue != null && fieldValue instanceof String) {
			String value = (String) fieldValue;
			if (value.contains("<") || value.contains(">")) {
				return false;
			}
		}

		return true;
	}

	private static boolean isBlank(final CharSequence cs) {

		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}

		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	private static boolean isEmpty(final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	private static boolean isEmpty(final Object[] array) {
		return array == null || array.length == 0;
	}
}
