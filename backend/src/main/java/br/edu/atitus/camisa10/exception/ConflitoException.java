package br.edu.atitus.camisa10.exception;

/**
 * Conflito com um dado já existente (ex.: e-mail já cadastrado). Vira HTTP 409.
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
