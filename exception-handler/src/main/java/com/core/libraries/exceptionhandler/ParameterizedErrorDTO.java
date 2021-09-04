package com.core.libraries.exceptionhandler;

import java.io.Serializable;

public class ParameterizedErrorDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int status;
	private final String message;
	private final String description;
	private final String[] params;

	public ParameterizedErrorDTO(int status, String message, String description, String[] params) {
		this.status = status;
		this.message = message;
		this.description = description;
		this.params = params;
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

	public String[] getParams() {
		return params;
	}

}
