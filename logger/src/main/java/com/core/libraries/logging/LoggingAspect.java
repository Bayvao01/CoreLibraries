package com.core.libraries.logging;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private static Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	/**
	 * Pointcut that matches all repositories, services and Web REST endpoints.
	 */
	@Pointcut("within(in..*) || within(com..*) || execution(@EnableLogging * *.*(..))")
	public void loggingPointcut() {
		// Method is empty as this is just a Pointcut, the implementation are in the
		// advices.
	}

	/**
	 * Advice that logs method throwing exception
	 * 
	 * @param joinPoint
	 * @param e
	 */
	@AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
		if (e.getCause() != null) {
			while (e.getCause() != null)
				e = e.getCause();

			logger.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), e);
		} else {
			logger.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), e);
		}
	}

	/**
	 * Advice that logs when a method is entered or exited.
	 * 
	 * @param joinPoint
	 * @param enableLogging
	 * @return
	 * @throws Throwable
	 */
	@Around("execution(@EnableLogging * *.*(..)) && @annotation(enableLogging)")
	public Object logAroundMethods(ProceedingJoinPoint joinPoint, EnableLogging enableLogging) throws Throwable { // NOSONAR

		if (logger.isDebugEnabled()) {
			logger.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		}
		try {
			Object result = joinPoint.proceed();
			if (logger.isDebugEnabled()) {
				if (result instanceof byte[]) {
					logger.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
							joinPoint.getSignature().getName(), "byte[" + ((byte[]) result).length + "]");
				} else {
					logger.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
							joinPoint.getSignature().getName(), result);
				}
			}
			return result;

		} catch (IllegalArgumentException e) { // NOSONAR
			logger.error("Illegal argument: {} in {}.{}() due to cause = {}", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
					e.getMessage());
			throw e;
		} catch (Throwable e) { // NOSONAR
			if (logger.isTraceEnabled()) {
				logger.error("Exception in {}.{}() due to cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getDeclaringTypeName(), e);
			}
			logAfterThrowing(joinPoint, e);
			throw e;
		}
	}
}
