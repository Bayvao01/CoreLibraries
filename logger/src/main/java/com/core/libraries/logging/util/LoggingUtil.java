package com.core.libraries.logging.util;

import com.core.libraries.logging.custom.CustomLogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import static com.core.libraries.logging.constants.LoggingConstants.*;

@Component
public class LoggingUtil {

    private static final Logger log = LoggerFactory.getLogger(LoggingUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private CustomLogUtil customLogUtil;

    private LoggingUtil(){

    }

    public StringBuffer appendClassAndMethodNames(ProceedingJoinPoint joinPoint){
        StringBuffer requestMessage = new StringBuffer();
        requestMessage.append(SPACE).append(CLASS_NAME).append(EQUALS).append(joinPoint.getTarget().getClass().getName())
                .append(SPACE).append(METHOD_NAME).append(EQUALS).append(joinPoint.getSignature().getName());

        return requestMessage;
    }

    public String errorMessage(String event, Tier tier, ProceedingJoinPoint joinPoint, long timeTaken,
                               String httpStatusCode){
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(EVENT).append(EQUALS).append(event).append(SPACE)
                .append(TIER).append(EQUALS).append(tier).append(SPACE)
                .append(IS_SUCCESS).append(EQUALS).append(N).append(SPACE)
                .append(ERROR_CODE).append(EQUALS).append(MDC.get(ERROR_CODE)).append(SPACE)
                .append(ERROR_MESSAGE).append(EQUALS).append(MDC.get(ERROR_MESSAGE)).append(SPACE)
                .append(this.appendClassAndMethodNames(joinPoint)).append(SPACE)
                .append(TIME_TAKEN).append(EQUALS).append(timeTaken).append(SPACE)
                .append(this.customLogUtil.checkCustomLog());

        if(ObjectUtils.isEmpty(httpStatusCode)){
            errorMessage.append(SPACE).append(HTTP_STATUS_CODE).append(httpStatusCode);
        }

        return errorMessage.toString();
    }

    public String customErrorMessage(String event, Tier tier, ProceedingJoinPoint joinPoint, long timeTaken,
                                     String httpStatusCode, String errorCode, String customErrorMessage){

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(EVENT).append(EQUALS).append(event).append(SPACE)
                .append(TIER).append(EQUALS).append(tier).append(SPACE)
                .append(IS_SUCCESS).append(EQUALS).append(N).append(SPACE)
                .append(ERROR_CODE).append(EQUALS).append(errorCode).append(SPACE)
                .append(ERROR_MESSAGE).append(EQUALS).append(customErrorMessage).append(SPACE)
                .append(this.appendClassAndMethodNames(joinPoint)).append(SPACE)
                .append(TIME_TAKEN).append(EQUALS).append(timeTaken).append(SPACE)
                .append(this.customLogUtil.checkCustomLog());

        if(!ObjectUtils.isEmpty(httpStatusCode)){
            errorMessage.append(SPACE).append(HTTP_STATUS_CODE).append(EQUALS).append(httpStatusCode);
        }

        return errorMessage.toString();
    }

    public String toJsonString(Object objectToConvertToJsonString){
        String objectAsString;

        if(!ObjectUtils.isEmpty(objectToConvertToJsonString)){
            String errorMsg;
            try{
                this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
                objectAsString = this.mapper.writeValueAsString(objectToConvertToJsonString);
            }catch (JsonProcessingException ex){
                errorMsg = "PARSING_ERROR LoggingUtil JsonProcessingException FAILED PARSING JSON RESPONSE";
                log.error(errorMsg);
                objectAsString = errorMsg;
            } catch (Throwable throwable){
                errorMsg = "PARSING_ERROR LoggingUtil Throwable FAILED PARSING JSON RESPONSE";
                log.error(errorMsg);
                objectAsString = errorMsg;
            }
        } else {
           String errorMsg = "PARSING_ERROR LoggingUtil objectToConvertToJsonString is NULL OR EMPTY";
            log.error(errorMsg);
            objectAsString = errorMsg;
        }

        return objectAsString;
    }

    public static void logErrorCodeAndErrorMessage(String errorCode, String errorMessage){
        if(!ObjectUtils.isEmpty(errorCode)){
            MDC.put(ERROR_CODE, errorCode);
            MDC.put(ERROR_MESSAGE, errorMessage != null ? errorMessage : "");
        } else {
            log.warn("logErrorCodeAndErrorMessage errorCode is empty or null ");
        }
    }
}
