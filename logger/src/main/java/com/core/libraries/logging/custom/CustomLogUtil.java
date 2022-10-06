package com.core.libraries.logging.custom;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

@Component
public class CustomLogUtil {

    public String checkCustomLog(){
        return CustomLogContext.getCustomLog() != null ? " "
                + this.convertMapToString(CustomLogContext.getCustomLog()) : "";
    }

    public String convertMapToString(Map<String, String> customMapLog){
        StringJoiner customLog = new StringJoiner(" ");
        Set<Map.Entry<String, String>> customMapEntrySet = customMapLog.entrySet();

        Iterator customMapInterator = customMapEntrySet.iterator();

        while (customMapInterator.hasNext()){
            Map.Entry<String, String> customMapEntry = (Map.Entry)customMapInterator.next();
            String var1001 = (String) customMapEntry.getKey();
            customLog.add(var1001 + "=" + (String)customMapEntry.getValue());
        }

        return customLog != null ? customLog.toString() : "";
    }
}
