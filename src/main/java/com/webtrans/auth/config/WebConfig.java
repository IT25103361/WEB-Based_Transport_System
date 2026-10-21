package com.webtrans.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**"); // Protect all paths
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/emergency/**")
                .addResourceLocations("classpath:/static/emergency/");
        registry.addResourceHandler("/courier/**")
                .addResourceLocations("classpath:/static/courier/");
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/courier/css/", "classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/courier/js/", "classpath:/static/js/");
        registry.addResourceHandler("/foodgrocery/**")
                .addResourceLocations("classpath:/static/foodgrocery/");
        registry.addResourceHandler("/food/uploads/**")
                .addResourceLocations("classpath:/static/foodgrocery/uploads/", "file:src/main/resources/static/foodgrocery/uploads/", "file:uploads/food/");
    }
}
