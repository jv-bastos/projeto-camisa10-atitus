package br.edu.atitus.camisa10.dto.vendedor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.atitus.camisa10.dto.pedido.ItemPedidoResponse;
import br.edu.atitus.camisa10.model.StatusPedido;

/**
 * Uma venda no histórico do vendedor (RF07).
 * Um pedido pode ter camisas de vários vendedores: aqui aparecem só os itens
 * DESTE vendedor, e "valor" é a soma apenas desses itens.
 * Cada item traz a personalização para a camisa ser produzida corretamente.
 */
public record VendaResponse(
        Long pedidoId,
        LocalDateTime dataCriacao,
        StatusPedido status,
        String compradorNome,
        BigDecimal valor,
        List<ItemPedidoResponse> itens) {
}
