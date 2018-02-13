package com.vaadin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by Ayhan.Ugurlu on 12/02/2018
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
public class QuichticketsDashboard {

    public static void main(String[] args) {
        SpringApplication.run(QuichticketsDashboard.class, args);
    }
}
