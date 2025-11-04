package com.automacao.acesso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class AcessoApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcessoApplication.class, args);
    }

}
