package br.edu.atitus.camisa10.dto.pedido;

import br.edu.atitus.camisa10.model.Personalizacao;

public record PersonalizacaoResponse(String nome, Integer numero) {

    /** Devolve null quando o item não foi personalizado. */
    public static PersonalizacaoResponse de(Personalizacao personalizacao) {
        return personalizacao == null ? null
                : new PersonalizacaoResponse(personalizacao.getNome(), personalizacao.getNumero());
    }
}
