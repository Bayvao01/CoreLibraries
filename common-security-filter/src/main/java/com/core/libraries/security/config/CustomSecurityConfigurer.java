package com.core.libraries.security.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import java.util.Iterator;
import java.util.Map;

import static com.core.libraries.security.constants.SecurityConstants.SEPARATOR_REGEX;

@Data
@Configuration
public class CustomSecurityConfigurer {

    @Value("#{${com.food.swipe.publicURIs:{:}}}")
    private Map<String, String> publicURIs;

    @Value(("#{${com.food.swipe.techinalURIs:{'/**/actuator/**':'GET','/swagger-ui/**':'GET'}}}"))
    private Map<String, String> publicTechnicalURIs;

    @Bean
    public MultiValueMap<String, String> allListedPublicURIs(){
        MultiValueMap<String, String> allPublicURIs = new LinkedMultiValueMap<>();

        Iterator iterator;
        Map.Entry currentEntry;
        String[] allListedURIs;
        int len;
        String currentHttpMethod;

        if(!ObjectUtils.isEmpty(this.publicURIs)){
            iterator = this.publicURIs.entrySet().iterator();

            while (iterator.hasNext()){
                currentEntry = (Map.Entry) iterator.next();
                allListedURIs = ((String)currentEntry.getValue()).split(SEPARATOR_REGEX);
                len = allListedURIs.length;

                for (int i = 0; i < len; ++i){
                    currentHttpMethod = allListedURIs[i];
                    allPublicURIs.add((String) currentEntry.getKey(), currentHttpMethod);
                }
            }
        }

        if(!ObjectUtils.isEmpty(publicTechnicalURIs)){
            iterator = this.publicTechnicalURIs.entrySet().iterator();

            while (iterator.hasNext()){
                currentEntry = (Map.Entry) iterator.next();
                allListedURIs = ((String)currentEntry.getValue()).split(",");
                len = allListedURIs.length;

                for (int i = 0; i < len; ++i){
                    currentHttpMethod = allListedURIs[i];
                    allPublicURIs.add((String) currentEntry.getKey(), currentHttpMethod);
                }
            }

        }

        return  allPublicURIs;
    }

}
