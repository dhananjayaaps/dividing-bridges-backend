package org.penpal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class InvitationRunner {
    public static void main(String []args) {
        SpringApplication.run(InvitationRunner.class,args);
    }

    @LoadBalanced
    @Bean(name = "invitationRestTemplate")
    @Primary
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}