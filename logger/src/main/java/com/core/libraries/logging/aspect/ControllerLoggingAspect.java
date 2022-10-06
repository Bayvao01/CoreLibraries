package com.core.libraries.logging.aspect;

import com.core.libraries.exceptionhandler.CustomException;
import com.core.libraries.logging.util.LoggingUtil;
import com.core.libraries.logging.util.Tier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.core.libraries.logging.constants.LoggingConstants.*;

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private LoggingUtil loggingUtil;

    @Around("@annotation(com.core.libraries.logging.annotation.LogControllerEntryExit)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object response = null;
        String httpStatusCode = null;
        long startTime = System.currentTimeMillis();

        ResponseEntity responseEntity = null;

        try{
            StringBuffer requestAndResponseDetails = new StringBuffer();
            if(log.isDebugEnabled()){
                requestAndResponseDetails = this.captureRequest(joinPoint);
            }

            HttpServletRequest servletRequest = ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();

            response = joinPoint.proceed();

            if(response != null && response instanceof ResponseEntity) {
                responseEntity = (ResponseEntity) response;

                if (responseEntity != null){
                    httpStatusCode = String.valueOf(responseEntity.getStatusCodeValue());
                }
            }

            if(log.isDebugEnabled()){
                String parsedResponse = this.captureResponse(response);
                requestAndResponseDetails.append(SPACE).append(RESPONSE).append(EQUALS).append(parsedResponse);

                log.debug(requestAndResponseDetails.toString());
            }

            if (response != null && response instanceof ResponseEntity){
                long stopTime = System.currentTimeMillis();
                Long timeTaken = stopTime - startTime;
                this.instrumentAndLog(joinPoint, timeTaken, httpStatusCode, servletRequest);
            }

            return response;

        } catch (Throwable throwable){
            long stopTime = System.currentTimeMillis();
            long timeTaken = stopTime - startTime;
            if (responseEntity != null){
                httpStatusCode = String.valueOf(responseEntity.getStatusCodeValue());
            } else {
                httpStatusCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }

            if (null != throwable){
                if (throwable instanceof CustomException) {
                    log.error(this.loggingUtil.customErrorMessage(ENTRY_EXIT, Tier.RESTAPI, joinPoint, timeTaken, httpStatusCode,
                           String.valueOf(((CustomException) throwable).getErrorDTO().getStatus()), ((CustomException) throwable).getErrorDTO().getMessage()));
                } else {
                    log.error(this.loggingUtil.errorMessage(ENTRY_EXIT, Tier.RESTAPI, joinPoint, timeTaken, httpStatusCode));
                }
            }

            throw throwable;
        }
    }

    private String captureResponse(Object response){
        return this.applyWriteValuesAsString(response);
    }

    public StringBuffer captureRequest(ProceedingJoinPoint joinPoint) throws JsonProcessingException {
        StringBuffer requestMessage = new StringBuffer();
        if (log.isDebugEnabled()){
            String parsedRequest = "";
            if(!ObjectUtils.isEmpty(joinPoint.getArgs())) {
                parsedRequest = (String) Arrays.stream(joinPoint.getArgs()).map(this::applyWriteValuesAsString).collect(Collectors.joining(","));
            }

            requestMessage.append(EVENT).append(EQUALS).append(JSON_DATA).append(SPACE)
                    .append(TIER).append(EQUALS).append(Tier.RESTAPI).append(" ")
                    .append(this.loggingUtil.appendClassAndMethodNames(joinPoint)).append(SPACE)
                    .append(REQUEST).append(EQUALS).append(ObjectUtils.isEmpty(parsedRequest) ? "empty" : parsedRequest);
        }

        return requestMessage;
    }

    public void instrumentAndLog(ProceedingJoinPoint joinPoint, Long timeTaken, String httpStatusCode,
                                 HttpServletRequest servletRequest) throws JsonProcessingException {

        StringBuffer responseMessage = new StringBuffer();

        responseMessage.append(EVENT).append(EQUALS).append(ENTRY_EXIT).append(SPACE)
                .append(TIER).append(EQUALS).append(Tier.RESTAPI).append(SPACE)
                .append(IS_SUCCESS).append(EQUALS).append(Y).append(SPACE)
                .append(this.loggingUtil.appendClassAndMethodNames(joinPoint)).append(SPACE)
                .append(TIME_TAKEN).append(EQUALS).append(timeTaken).append(SPACE)
                .append(HTTP_CODE).append(EQUALS).append(httpStatusCode);

        log.info(responseMessage.toString(), METHOD_NAME + EQUALS, joinPoint.getSignature().getName(), ", " + TIME_TAKEN + EQUALS + timeTaken);
    }

    private String applyWriteValuesAsString(Object responseAgrs){
        String errorMsg;

        try{
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return  mapper.writeValueAsString(responseAgrs);
        } catch (JsonProcessingException ex){
            errorMsg = "ControllerLoggingAspect JsonProcessingException FAILED PARSING JSON RESPONSE";
            log.error(errorMsg);
            return errorMsg;
        } catch (Throwable throwable){
            errorMsg = "ControllerLoggingAspect Throwable FAILED PARSING JSON RESPONSE";
            log.error(errorMsg);
            return errorMsg;
        }
    }
}
