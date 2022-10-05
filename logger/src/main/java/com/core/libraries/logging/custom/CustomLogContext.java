package com.core.libraries.logging.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomLogContext {

    private CustomLogContext(){

    }

    private static final InheritableThreadLocal<Map<String, String>> logContext = new InheritableThreadLocal<>();

    public static void setCustomLog(Map<String, String> customLog){
        logContext.set(customLog);
    }

    public static Map getCustomLog(){
        if(logContext.get() == null){
            logContext.set(new ConcurrentHashMap<>());
        }

        return (Map)logContext.get();
    }

    public static void removeCustomLog(){
        logContext.remove();
    }
}
