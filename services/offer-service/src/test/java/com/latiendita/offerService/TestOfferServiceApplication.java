package com.latiendita.offerService;

import org.springframework.boot.SpringApplication;

public class TestOfferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(OfferServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
