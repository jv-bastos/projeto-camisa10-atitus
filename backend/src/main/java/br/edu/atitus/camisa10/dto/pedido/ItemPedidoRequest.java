package br.edu.atitus.camisa10.dto.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import br.edu.atitus.camisa10.model.Tamanho;

/**
 * Um item do carrinho enviado na compra.
 * personalizacao é opcional: null = camisa sem nome/número.
 * O preço NÃO é enviado pelo app: o backend usa o preço atual do produto.
 */
public record ItemPedidoRequest(

        @NotNull(message = "Informe o produto.")
        Long produtoId,

        @NotNull(message = "Escolha o tamanho.")
        Tamanho tamanho,

        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade mínima é 1.")
        Integer quantidade,

        PersonalizacaoRequest personalizacao) {
}
