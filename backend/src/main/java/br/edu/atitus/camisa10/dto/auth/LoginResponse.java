package br.edu.atitus.camisa10.dto.auth;

/** Resposta do login e do cadastro: { token, usuario }. */
public record LoginResponse(String token, UsuarioResponse usuario) {
}
