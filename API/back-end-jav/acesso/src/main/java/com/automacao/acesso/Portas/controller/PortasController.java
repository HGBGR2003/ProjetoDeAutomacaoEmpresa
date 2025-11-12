package com.automacao.acesso.Portas.controller;

import com.automacao.acesso.Portas.model.Portas;
import com.automacao.acesso.Portas.service.PortasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portas")
public class PortasController {

    @Autowired
    private PortasService service;

    @PostMapping
    public ResponseEntity<Portas> criar(@RequestBody Portas porta) {
        Portas nova = service.salvar(porta);
        return ResponseEntity.ok(nova);
    }

    @GetMapping
    public ResponseEntity<List<Portas>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Portas> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping()
    public ResponseEntity<Portas> atualizar(@RequestBody Portas portaAtualizada) {
        try {
            Portas atualizada = service.atualizar(portaAtualizada);
            return ResponseEntity.ok(atualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}