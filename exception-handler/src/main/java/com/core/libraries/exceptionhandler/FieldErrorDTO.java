package com.core.libraries.exceptionhandler;

import java.io.Serializable;

/*
 * Field Binding Error to be returned to client
 */
public class FieldErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String objectName;
	private final String field;
	private final String message;

	FieldErrorDTO(String objectName, String field, String message) {
		this.objectName = objectName;
		this.field = field;
		this.message = message;
	}

	public String getObjectName() {
		return objectName;
	}

	public String getField() {
		return field;
	}

	public String getMessage() {
		return message;
	}

}
