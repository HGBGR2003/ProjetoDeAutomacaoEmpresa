package com.automacao.acesso.Pessoa.repository;

import com.automacao.acesso.Pessoa.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RP extends JpaRepository<Pessoa, Long> {
    Optional<Pessoa> findByEmail(String email);
}