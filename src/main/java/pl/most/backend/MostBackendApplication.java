package pl.most.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MostBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MostBackendApplication.class, args);

    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Pozwól na połączenia ze wszystkich originów (do testów lokalnych)
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }

}
