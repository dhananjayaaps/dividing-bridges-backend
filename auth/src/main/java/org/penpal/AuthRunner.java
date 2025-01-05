package org.penpal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AuthRunner {
    public static void main(String []args) {
        SpringApplication.run(AuthRunner.class,args);
    }

    @Primary
    @Bean(name = "authRestTemplate")
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}