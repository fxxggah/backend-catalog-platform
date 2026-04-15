package com.catalog.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "SEU_CLOUD_NAME",
                "api_key", "SEU_API_KEY",
                "api_secret", "SEU_API_SECRET"
        ));
    }
}