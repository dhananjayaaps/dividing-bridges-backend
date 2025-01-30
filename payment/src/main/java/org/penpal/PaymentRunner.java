package org.penpal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class PaymentRunner {
    public static void main(String []args) {
        SpringApplication.run(PaymentRunner.class,args);
    }

    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}