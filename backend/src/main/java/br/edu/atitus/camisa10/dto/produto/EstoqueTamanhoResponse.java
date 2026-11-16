package br.edu.atitus.camisa10.dto.produto;

import java.util.Arrays;
import java.util.List;

import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;

/** Estoque de um tamanho. O produto sempre devolve a grade completa, na ordem P, M, G, GG, XGG. */
public record EstoqueTamanhoResponse(Tamanho tamanho, int quantidade) {

    public static List<EstoqueTamanhoResponse> grade(Produto produto) {
        return Arrays.stream(Tamanho.values())
                .map(t -> new EstoqueTamanhoResponse(t, produto.getEstoque(t)))
                .toList();
    }
}
