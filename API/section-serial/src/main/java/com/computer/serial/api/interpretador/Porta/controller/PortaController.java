package com.computer.serial.api.interpretador.Porta.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portas")
public class PortaController {
    @GetMapping("/1")
    public String getStatusPorta1() {
        System.out.println("Recebi uma requisição na API!");
        return "LIGADO"; 
    }
}
