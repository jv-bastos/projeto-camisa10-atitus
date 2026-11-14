package br.edu.atitus.camisa10.exception;

/**
 * Violação de uma regra de negócio (ex.: estoque insuficiente).
 * O handler global devolve HTTP 400 com a mensagem.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
