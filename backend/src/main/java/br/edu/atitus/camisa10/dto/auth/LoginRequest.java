package br.edu.atitus.camisa10.dto.auth;

import jakarta.validation.constraints.NotBlank;

/** Dados enviados no login (RF02). */
public record LoginRequest(

        @NotBlank(message = "Informe o e-mail.")
        String email,

        @NotBlank(message = "Informe a senha.")
        String senha) {

    public LoginRequest {
        email = EmailUtil.normalizar(email);
    }
}
