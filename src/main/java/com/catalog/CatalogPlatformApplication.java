package com.catalog;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatalogPlatformApplication {

	public static void main(String[] args) {

		Dotenv dotenv = Dotenv.load();

		System.setProperty("CLOUDINARY_CLOUD_NAME", dotenv.get("CLOUDINARY_CLOUD_NAME"));
		System.setProperty("CLOUDINARY_API_KEY", dotenv.get("CLOUDINARY_API_KEY"));
		System.setProperty("CLOUDINARY_API_SECRET", dotenv.get("CLOUDINARY_API_SECRET"));

		System.setProperty("DB_HOST", dotenv.get("DB_HOST"));
		System.setProperty("DB_PORT", dotenv.get("DB_PORT"));
		System.setProperty("DB_NAME", dotenv.get("DB_NAME"));
		System.setProperty("DB_USER", dotenv.get("DB_USER"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		System.out.println("CLOUDINARY_CLOUD_NAME: " + dotenv.get("CLOUDINARY_CLOUD_NAME"));
		System.out.println("CLOUDINARY_API_KEY: " + dotenv.get("CLOUDINARY_API_KEY"));
		System.out.println("CLOUDINARY_API_SECRET: " + dotenv.get("CLOUDINARY_API_SECRET"));
		System.out.println("DB_HOST: " + dotenv.get("DB_HOST"));
		System.out.println("DB_PORT: " + dotenv.get("DB_PORT"));
		System.out.println("DB_NAME: " + dotenv.get("DB_NAME"));
		System.out.println("DB_USER: " + dotenv.get("DB_USER"));
		System.out.println("DB_PASSWORD: " + dotenv.get("DB_PASSWORD"));

		SpringApplication.run(CatalogPlatformApplication.class, args);
	}
}