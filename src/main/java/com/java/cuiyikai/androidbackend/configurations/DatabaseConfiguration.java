package com.java.cuiyikai.androidbackend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * <p>The configuration class to load the settings for database.</p>
 * <p>Edit these values in application.yml</p>
 */
@Configuration
@PropertySource("classpath:application.yml")
public class DatabaseConfiguration {

    @Value("${spring.datasource.driver-class-name}")
    public String driverClassName;

    @Value("${spring.datasource.url}")
    public String url;

    @Value("${spring.datasource.username}")
    public String username;

    @Value("${spring.datasource.password}")
    public String password;
}
