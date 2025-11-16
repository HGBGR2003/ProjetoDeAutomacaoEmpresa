package com.automacao.acesso.Autenticacao.service;

import com.automacao.acesso.Pessoa.model.Pessoa;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String gerarToken(Pessoa pessoa) {
        try {
            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            Instant agora = LocalDateTime.now().toInstant(ZoneOffset.of("-03:00"));
            Instant dataExpiracao = agora.plusSeconds(300);

            return Jwts.builder()
                    .setIssuer("API Acesso")
                    .setSubject(pessoa.getUsername())
                    .setIssuedAt(Date.from(agora))
                    .setExpiration(Date.from(dataExpiracao))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT", e);
        }
    }

    public String getSubject(String tokenJWT) {
        try {
            Key secretKey = Keys.hmacShaKeyFor(secret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(tokenJWT)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}