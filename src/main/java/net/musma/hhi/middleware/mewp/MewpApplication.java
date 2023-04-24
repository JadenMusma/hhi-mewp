package net.musma.hhi.middleware.mewp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MewpApplication {

    public static void main(String[] args) {
        SpringApplication.run(MewpApplication.class, args);
    }

}
