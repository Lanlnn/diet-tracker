package com.diettracker.config;

import com.diettracker.admin.AdminSecurityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
 public class WebConfig implements WebMvcConfigurer {
     @Value("${app.upload.dir}")
     private String uploadDir;

     @Value("${app.cors.allowed-origins}")
     private String[] allowedOrigins;

     private final AdminSecurityInterceptor adminSecurityInterceptor;

     public WebConfig(AdminSecurityInterceptor adminSecurityInterceptor) {
         this.adminSecurityInterceptor = adminSecurityInterceptor;
     }

     @Override
     public void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(adminSecurityInterceptor).addPathPatterns("/api/admin/**");
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/uploads/avatars/**")
                 .addResourceLocations("file:" + uploadDir + "/");
     }
 
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns(allowedOrigins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }

}
