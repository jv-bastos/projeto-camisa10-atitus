package br.edu.atitus.camisa10.dto.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Um item do carrinho enviado na compra.
 * personalizacao é opcional: null = camisa sem nome/número.
 * O preço NÃO é enviado pelo app: o backend usa o preço atual do produto.
 */
public record ItemPedidoRequest(

        @NotNull(message = "Informe o produto.")
        Long produtoId,

        @NotNull(message = "Informe a quantidade.")
        @Min(value = 1, message = "A quantidade mínima é 1.")
        Integer quantidade,

        PersonalizacaoRequest personalizacao) {
}
