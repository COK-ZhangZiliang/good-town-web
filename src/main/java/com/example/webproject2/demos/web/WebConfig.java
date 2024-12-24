package com.example.webproject2.demos.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射：将 /uploads/** 映射到本地上传文件夹
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/Users/ziliang/IdeaProjects/good-town-web/uploads/");
    }
}