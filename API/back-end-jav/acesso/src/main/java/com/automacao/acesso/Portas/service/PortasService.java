package com.automacao.acesso.Portas.service;
import com.automacao.acesso.Portas.model.Portas;
import com.automacao.acesso.Portas.repository.PortasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PortasService {

    @Autowired
    private PortasRepository repository;

    public Portas salvar(Portas porta) {
        return repository.save(porta);
    }

    public List<Portas> listarTodas() {
        return repository.findAll();
    }

    public Optional<Portas> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Portas atualizar(Long id, Portas novaPorta) {
        return repository.findById(id)
                .map(porta -> {
                    porta.setDescricao(novaPorta.getDescricao());
                    porta.setStatus(novaPorta.getStatus());
                    return repository.save(porta);
                })
                .orElseThrow(() -> new RuntimeException("Porta não encontrada"));
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}