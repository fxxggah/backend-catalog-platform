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
                "cloud_name", "dmpenlfjw",
                "api_key", "246228352825179",
                "api_secret", "uwf6s-uqv7mM0-hlIMpqZS-s5Ag"
        ));
    }
}