package com.example.demo.configuration;

import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Log4j2
@Configuration
@Scope("singleton")
public class ApplicationConfiguration {

    static {

    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
