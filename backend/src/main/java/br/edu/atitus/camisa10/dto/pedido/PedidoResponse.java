package br.edu.atitus.camisa10.dto.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.atitus.camisa10.model.Pedido;
import br.edu.atitus.camisa10.model.StatusPedido;

/** Pedido no histórico do comprador (RF14). */
public record PedidoResponse(
        Long id,
        LocalDateTime dataCriacao,
        StatusPedido status,
        BigDecimal valorTotal,
        List<ItemPedidoResponse> itens) {

    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getDataCriacao(), pedido.getStatus(),
                pedido.getValorTotal(), pedido.getItens().stream().map(ItemPedidoResponse::de).toList());
    }
}
