package com.core.libraries.logging.aspect;

import com.core.libraries.exceptionhandler.CustomException;
import com.core.libraries.logging.custom.CustomLogUtil;
import com.core.libraries.logging.util.LoggingUtil;
import com.core.libraries.logging.util.Tier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.core.libraries.logging.constants.LoggingConstants.*;

@Aspect
@Component
public class LogEntryExitAspect {

    private static final Logger log = LoggerFactory.getLogger(LogEntryExitAspect.class);

    @Autowired
    private LoggingUtil loggingUtil;

    @Autowired
    private CustomLogUtil customLogUtil;

    @Around("@annotation(com.core.libraries.logging.annotation.LogEntryExit)")
    public Object logSomethingAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object response;

        try{

            response = joinPoint.proceed();
        } catch (Exception ex) {
            long stopTime = System.currentTimeMillis();
            long timeTaken = stopTime - startTime;

            if(ex instanceof CustomException) {
                log.error(this.loggingUtil.customErrorMessage(ENTRY_EXIT, Tier.RESTAPI, joinPoint, timeTaken, null,
                        String.valueOf(((CustomException) ex).getErrorDTO().getStatus()), ((CustomException) ex).getErrorDTO().getMessage()), ex);
            } else{
                log.error(this.loggingUtil.errorMessage(ENTRY_EXIT, Tier.SERVICE, joinPoint, timeTaken, null), ex);
            }

            throw ex;
        }

        long stopTime = System.currentTimeMillis();
        long timeTaken = stopTime - startTime;

        if (log.isDebugEnabled()){
            this.logResponse(joinPoint, timeTaken);
        }

        return response;
    }

    private void logResponse(ProceedingJoinPoint joinPoint, long timeTaken) {
        StringBuffer responseMessage = new StringBuffer();

        responseMessage.append(EVENT).append(EQUALS).append(ENTRY_EXIT).append(SPACE)
                .append(TIER).append(EQUALS).append(Tier.SERVICE).append(SPACE)
                .append(IS_SUCCESS).append(EQUALS).append(Y).append(SPACE)
                .append(this.loggingUtil.appendClassAndMethodNames(joinPoint)).append(SPACE)
                .append(TIME_TAKEN).append(EQUALS).append(timeTaken).append(SPACE)
                .append(this.customLogUtil.checkCustomLog());

        log.debug(responseMessage + " " + METHOD_NAME + EQUALS, joinPoint.getSignature().getName(), ", " + TIME_TAKEN + EQUALS + timeTaken);

    }

}
