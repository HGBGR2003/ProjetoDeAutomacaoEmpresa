package com.automacao.acesso.Portas.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.automacao.acesso.Portas.model.Portas;

public interface PortasRepository  extends JpaRepository<Portas, Long> {
    
}
