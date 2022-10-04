package com.core.libraries;

import java.io.Serializable;

/**
 * @author Bayvao Verma
 *
 */
public class FieldValidationError implements Serializable {

	private static final long serialVersionUID = 1L;

	private String field;
	private String message;

	protected FieldValidationError(String field, String message) {
		this.field = field;
		this.message = message;
	}

	public String getField() {
		return field;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "FieldValidationError [field=" + field + ", message=" + message + "]";
	}

}
