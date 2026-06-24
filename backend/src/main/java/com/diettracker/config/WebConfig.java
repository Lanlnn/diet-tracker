package com.diettracker.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
 public class WebConfig implements WebMvcConfigurer {
     @Value("${app.upload.dir:uploads}")
     private String uploadDir;
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/uploads/**")
                 .addResourceLocations("file:" + uploadDir + "/");
     }
 
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }

    @Bean
    public Filter requestLoggingFilter() {
        return new Filter() {
            private final Logger log = LoggerFactory.getLogger("HTTP_TRACE");

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
                try {
                    HttpServletRequest req = (HttpServletRequest) request;
                    ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(req);
                    chain.doFilter(wrapped, response);
                    if (req.getRequestURI().startsWith("/api/") && "POST".equals(req.getMethod())) {
                        byte[] buf = wrapped.getContentAsByteArray();
                        if (buf.length > 0) {
                            log.info(">>> POST {} body: {}", req.getRequestURI(), new String(buf, req.getCharacterEncoding()));
                        }
                    }
                } catch (Exception e) {
                    log.error("Logging filter error", e);
                }
            }
        };
    }
}
