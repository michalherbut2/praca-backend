package pl.most.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MostBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MostBackendApplication.class, args);

    }

}
