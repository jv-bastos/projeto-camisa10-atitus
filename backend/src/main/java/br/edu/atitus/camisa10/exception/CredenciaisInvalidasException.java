package br.edu.atitus.camisa10.exception;

/**
 * E-mail ou senha incorretos no login. Vira HTTP 401.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
