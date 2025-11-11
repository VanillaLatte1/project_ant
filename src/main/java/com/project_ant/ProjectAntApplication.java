package com.project_ant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootApplication
public class ProjectAntApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectAntApplication.class, args);
    }

    @Bean
    CommandLineRunner testOAuth(ClientRegistrationRepository repo) {
        return args -> {
            System.out.println("google: " + (repo.findByRegistrationId("google") != null));
            System.out.println("kakao: " + (repo.findByRegistrationId("kakao") != null));
            System.out.println("naver: " + (repo.findByRegistrationId("naver") != null));
        };
    }
}
