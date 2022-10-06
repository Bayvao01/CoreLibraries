package com.core.libraries.logging.aspect;

import com.core.libraries.logging.util.LoggingUtil;
import com.core.libraries.logging.util.Tier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class GraphQLOperationLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(GraphQLOperationLoggingAspect.class);

    @Autowired
    private LoggingUtil loggingUtil;

    @Around("@annotation(com.core.libraries.logging.annotation.LogGraphQLEntryExit)")
    public Object checkAndLogAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String httpStatusCode = String.valueOf(HttpStatus.OK.value());

        StringBuffer requestAndResponseDetails = new StringBuffer();

        if(log.isDebugEnabled()){
            requestAndResponseDetails = this.captureRequest(joinPoint);
        }

        Object resonse = null;

        long stopTime;
        long timeTaken;

        try {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes())).getRequest();

            resonse = joinPoint.proceed();

            if (log.isDebugEnabled()) {
                String responseAsString = this.captureResponse(resonse);
                requestAndResponseDetails.append(SPACE).append(RESPONSE).append(EQUALS).append(responseAsString);
                log.debug(requestAndResponseDetails.toString());
            }

            stopTime = System.currentTimeMillis();
            timeTaken = stopTime - startTime;

            this.instrumentAndLog(joinPoint, timeTaken, httpStatusCode, resonse, servletRequest);
            return resonse;

        } catch (Exception ex){
            stopTime = System.currentTimeMillis();
            timeTaken = stopTime - startTime;
            httpStatusCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());

            log.error(this.loggingUtil.errorMessage(ENTRY_EXIT, Tier.GRAPHQL, joinPoint, timeTaken, httpStatusCode), ex);

            throw ex;
        }
    }

    private String captureResponse(Object response){
        return this.loggingUtil.toJsonString(response);
    }

    public StringBuffer captureRequest(ProceedingJoinPoint joinPoint){
        StringBuffer requestMessage = new StringBuffer();

        if (log.isDebugEnabled()){
            String parseRequest = "";
            if (!ObjectUtils.isEmpty(joinPoint.getArgs())){
                parseRequest = (String) Arrays.stream(joinPoint.getArgs()).map( (args) -> {
                 return this.captureResponse(args);
                }).collect(Collectors.joining(","));
            }

            requestMessage.append(EVENT).append(EQUALS).append(JSON_DATA).append(SPACE)
                    .append(TIER).append(EQUALS).append(Tier.GRAPHQL).append(SPACE)
                    .append(this.loggingUtil.appendClassAndMethodNames(joinPoint)).append(SPACE)
                    .append(REQUEST).append(EQUALS).append(ObjectUtils.isEmpty(parseRequest) ? "empty" : parseRequest);
        }

        return requestMessage;
    }

    private void instrumentAndLog(ProceedingJoinPoint joinPoint, long timeTaken, String httpStatusCode,
                                  Object response, HttpServletRequest servletRequest){

        StringBuffer responseMessage = new StringBuffer();

        responseMessage.append(EVENT).append(EQUALS).append(ENTRY_EXIT).append(SPACE)
                .append(TIER).append(EQUALS).append(Tier.GRAPHQL).append(SPACE)
                .append(IS_SUCCESS).append(EQUALS).append(Y).append(SPACE)
                .append(this.loggingUtil.appendClassAndMethodNames(joinPoint)).append(SPACE)
                .append(TIME_TAKEN).append(EQUALS).append(timeTaken).append(SPACE)
                .append(HTTP_CODE).append(EQUALS).append(httpStatusCode);

        log.info(responseMessage.toString(),
                CLASS_NAME + EQUALS, joinPoint.getTarget().getClass().getName(),
                METHOD_NAME + EQUALS, joinPoint.getSignature().getName(),
                TIME_TAKEN + EQUALS, timeTaken);
    }

}
