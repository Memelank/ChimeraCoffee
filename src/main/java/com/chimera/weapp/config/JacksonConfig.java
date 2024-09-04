package com.chimera.weapp.config;

import com.chimera.weapp.vo.CoffeeVariant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        // Register your custom serializers here
        module.addSerializer(CoffeeVariant.class, new GenericSerializer<>());

        objectMapper.registerModule(module);
        return objectMapper;
    }
}