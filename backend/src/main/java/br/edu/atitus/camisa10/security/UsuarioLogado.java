package br.edu.atitus.camisa10.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lê o id do usuário logado a partir do token já validado pelo Spring Security.
 * O id fica na claim "sub" (ver TokenService). Nunca confiamos em um id enviado no corpo.
 */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Long id(Jwt token) {
        return Long.valueOf(token.getSubject());
    }
}
