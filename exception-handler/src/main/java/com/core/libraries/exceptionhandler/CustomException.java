package com.core.libraries.exceptionhandler;

/*
 * @author Bayvao Verma
 * @version 1.0
 * @since 2021-08-29
 * 
 */

/*
 * Custom, parameterized exception, which can be translated on the client side.
 * For example:
 * 
 * throw new CustomException("myCustomError", "hello", "world");
 * 
 * Can be translated with:
 * "error.myCustomError" : "The server says {{params[0]}} to {{params[1]}}"
 * 
 */

public class CustomException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final int status;
	private final String message;
	private final String description;
	private final String[] params;
	
	public CustomException(String message, String... params) {
		super(message);
		this.status = ErrorConstants.ERR_STATUS;
		this.message = message;
		this.params = params;
		this.description = "";
	}
	

	public CustomException(int status, String message, String... params) {
		super(message);
		this.status = status;
		this.message = message;
		this.params = params;
		this.description = "";
	}

	

	public CustomException(int status, String message, String description, String... params) {
		super(message);
		this.status = status;
		this.message = message;
		this.params = params;
		this.description = description;
	}
	
	public ParameterizedErrorDTO getErrorDTO() {
		return new ParameterizedErrorDTO(status, message, description, params);
	}


}
