package br.edu.atitus.camisa10.dto.pedido;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * Corpo de POST /api/pedidos:
 * { "itens": [ { "produtoId": 1, "quantidade": 2,
 *                "personalizacao": { "nome": "RONALDINHO", "numero": 10 } } ] }
 */
public record PedidoRequest(

        @NotEmpty(message = "Adicione pelo menos um item ao pedido.")
        List<@Valid ItemPedidoRequest> itens) {
}
