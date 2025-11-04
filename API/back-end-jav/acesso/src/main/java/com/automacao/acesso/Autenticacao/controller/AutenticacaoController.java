package com.automacao.acesso.Autenticacao.controller;

import com.automacao.acesso.Autenticacao.dto.DadosLoginDTO;
import com.automacao.acesso.Autenticacao.dto.DadosTokenDTO;
import com.automacao.acesso.Autenticacao.service.TokenService;
import com.automacao.acesso.Pessoa.model.Pessoa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity<DadosTokenDTO> efetuarLogin(@RequestBody DadosLoginDTO dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());

        Authentication authentication = manager.authenticate(authenticationToken);
        Pessoa pessoaAutenticada = (Pessoa) authentication.getPrincipal();

        String tokenJWT = tokenService.gerarToken(pessoaAutenticada);
        return ResponseEntity.ok(new DadosTokenDTO(tokenJWT));
    }
}