package br.edu.atitus.camisa10.exception;

/**
 * O usuário está logado, mas não pode mexer neste recurso
 * (ex.: vendedor tentando editar produto de outro vendedor – RF08). Vira HTTP 403.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
