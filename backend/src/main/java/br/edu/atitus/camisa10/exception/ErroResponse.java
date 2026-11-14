package br.edu.atitus.camisa10.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato padrão de erro devolvido pela API.
 *
 * Exemplo:
 * { "status": 400, "mensagem": "Dados inválidos.",
 *   "campos": { "email": "Informe um e-mail válido." },
 *   "timestamp": "2026-11-11T17:00:00" }
 *
 * "campos" só aparece em erros de validação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(int status, String mensagem, Map<String, String> campos, LocalDateTime timestamp) {

    public ErroResponse(int status, String mensagem) {
        this(status, mensagem, null, LocalDateTime.now());
    }

    public ErroResponse(int status, String mensagem, Map<String, String> campos) {
        this(status, mensagem, campos, LocalDateTime.now());
    }
}
