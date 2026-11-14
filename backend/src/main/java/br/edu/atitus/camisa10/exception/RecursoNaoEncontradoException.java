package br.edu.atitus.camisa10.exception;

/**
 * Registro não encontrado (ex.: produto inexistente). Vira HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
