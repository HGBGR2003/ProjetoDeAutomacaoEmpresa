package com.automacao.acesso.Pessoa.Services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.automacao.acesso.Pessoa.model.Pessoa;
import com.automacao.acesso.Pessoa.repository.RP;

@Service
public class Services {
    @Autowired
    private RP pessoaRepository;

    public Pessoa salvar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAll();
    }

    public Optional<Pessoa> buscarPorId(Long id) {
        return pessoaRepository.findById(id);
    }

    public Pessoa atualizar(Long id, Pessoa novaPessoa) {
        return pessoaRepository.findById(id)
                .map(pessoa -> {
                    pessoa.setNome(novaPessoa.getNome());
                    pessoa.setEmail(novaPessoa.getEmail());
                    pessoa.setSenha(novaPessoa.getSenha());
                    return pessoaRepository.save(pessoa);
                })
                .orElseThrow(() -> new RuntimeException("Pessoa não encontrada com ID: " + id));
    }

    public void deletar(Long id) {
        pessoaRepository.deleteById(id);
    }
}
