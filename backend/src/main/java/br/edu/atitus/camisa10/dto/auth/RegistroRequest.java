package br.edu.atitus.camisa10.dto.auth;

import br.edu.atitus.camisa10.model.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Dados enviados no cadastro (RF01). */
public record RegistroRequest(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String nome,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String senha,

        @NotNull(message = "Escolha o perfil: VENDEDOR ou COMPRADOR.")
        Perfil perfil) {

    /** Remove espaços extras (comuns no autocompletar do celular) antes da validação. */
    public RegistroRequest {
        nome = nome == null ? null : nome.trim();
        email = EmailUtil.normalizar(email);
    }
}
