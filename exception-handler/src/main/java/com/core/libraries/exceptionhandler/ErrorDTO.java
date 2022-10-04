package com.core.libraries.exceptionhandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bayvao Verma
 *
 */
public class ErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int status;
	private final String message;
	private final String description;
	private List<FieldErrorDTO> fieldErrors;

	ErrorDTO(int status) {
		this(status, "");
	}

	ErrorDTO(int status, String message) {
		this(status, message, null);
	}

	ErrorDTO(String message) {
		this(ErrorConstants.ERR_STATUS, message, null);
	}

	ErrorDTO(int status, String message, String description) {
		this(status, message, description, null);
	}

	ErrorDTO(int status, String message, String description, List<FieldErrorDTO> fieldErrors) {
		this.status = status;
		this.message = message;
		this.description = description;
		this.fieldErrors = fieldErrors;
	}

	public void add(String objectName, String field, String message) {
		if (fieldErrors == null) {
			fieldErrors = new ArrayList<>();
		}
		fieldErrors.add(new FieldErrorDTO(objectName, field, message));
	}

	public List<FieldErrorDTO> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(List<FieldErrorDTO> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String getDescription() {
		return description;
	}

}
