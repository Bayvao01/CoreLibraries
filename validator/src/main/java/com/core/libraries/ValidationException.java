package com.core.libraries;

import java.util.List;

/**
 * @author Bayvao Verma
 *
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int status;
	private final String description;
	private final List<FieldValidationError> validationErrors;

	public ValidationException(int status, String message, String description) {
		super(message);
		this.status = status;
		this.description = description;
		this.validationErrors = null;
	}

	public ValidationException(int status, String message, List<FieldValidationError> validationError) {
		super(message);
		this.status = status;
		this.description = "";
		this.validationErrors = validationError;
	}

	public List<FieldValidationError> getValidationErrors() {
		return validationErrors;
	}

	public int getStatus() {
		return status;
	}

	public String getDescription() {
		return description;
	}

}
