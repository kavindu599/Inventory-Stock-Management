package com.yourcompany.itemcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class ItemcrudApplication {

    public static void main(String[] args) {
        // Create the items.txt file if it doesn't exist
        try {
            File itemsFile = new File("items.txt");
            if (!itemsFile.exists()) {
                itemsFile.createNewFile();
                System.out.println("Created items.txt file");
            }
        } catch (IOException e) {
            System.err.println("Error creating items file: " + e.getMessage());
        }

        SpringApplication.run(ItemcrudApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}