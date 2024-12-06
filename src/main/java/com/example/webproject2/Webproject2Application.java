package com.example.webproject2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class Webproject2Application {

    public static void main(String[] args) {
        SpringApplication.run(Webproject2Application.class, args);
    }

}
