package com.core.libraries.exceptionhandler;

public final class ErrorConstants {

	public static final int ERR_STATUS = 400;
	public static final int ERR_RUNTIME_FAILURE_STATUS = 503;
	public static final int ERR_VALIDATION_STATUS = 400;
	public static final int ERR_UNAUTHORIZED_STATUS = 401;
	public static final int ERR_ACCESS_DENIED_STATUS = 403;
	public static final int ERR_CONCURRENCY_FAILURE_STATUS = 409;
	public static final int ERR_METHOD_NOT_SUPPORTED_STATUS = 405;
	
	public static final String ERR_RUNTIME_FAILURE = "We are unable to process your request. Please try later.";
	public static final String ERR_MAX_UPLOAD_SIZE_EXCEEDED = "Your request was rejected because uploaded file size exceeds maximum permitted size.";
	public static final String ERR_MULTIPART_FAILURE = "Your request was rejected because uploaded data is too large or invalid.";
	public static final String ERR_VALIDATION = "Validation failed";
	public static final String ERR_UNAUTHORIZED = "You are not authorized to perform this exception.";
	public static final String ERR_ACCESSS_DENIED = "You are not authorized to perform this exception.";
	public static final String ERR_CONCURRENCY_FAILURE = "The record you are working on has been modified by another user";
	public static final String ERR_METHOD_NOT_SUPPORTED = "We are unable to process your request. Please try later...";

}
