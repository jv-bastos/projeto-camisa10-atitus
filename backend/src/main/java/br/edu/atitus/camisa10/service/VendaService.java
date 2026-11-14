package br.edu.atitus.camisa10.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.dto.pedido.ItemPedidoResponse;
import br.edu.atitus.camisa10.dto.vendedor.ResumoVendedorResponse;
import br.edu.atitus.camisa10.dto.vendedor.VendaResponse;
import br.edu.atitus.camisa10.model.ItemPedido;
import br.edu.atitus.camisa10.model.Pedido;
import br.edu.atitus.camisa10.repository.ItemPedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;

/**
 * Dashboard (RF06) e histórico de vendas (RF07) do vendedor.
 * Considera apenas os itens de produtos do próprio vendedor.
 */
@Service
public class VendaService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;

    public VendaService(ItemPedidoRepository itemPedidoRepository, ProdutoRepository produtoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoVendedorResponse resumo(Long vendedorId) {
        BigDecimal faturamento = itemPedidoRepository.somarFaturamentoDoVendedor(vendedorId);
        return new ResumoVendedorResponse(
                produtoRepository.countByVendedorIdAndAtivoTrue(vendedorId),
                itemPedidoRepository.contarPedidosDoVendedor(vendedorId),
                faturamento == null ? BigDecimal.ZERO : faturamento);
    }

    /** Agrupa os itens vendidos por pedido, mantendo a ordem (mais recente primeiro). */
    @Transactional(readOnly = true)
    public List<VendaResponse> listarVendas(Long vendedorId) {
        Map<Pedido, List<ItemPedido>> itensPorPedido = new LinkedHashMap<>();
        for (ItemPedido item : itemPedidoRepository.buscarVendasDoVendedor(vendedorId)) {
            itensPorPedido.computeIfAbsent(item.getPedido(), p -> new ArrayList<>()).add(item);
        }

        List<VendaResponse> vendas = new ArrayList<>();
        itensPorPedido.forEach((pedido, itens) -> {
            BigDecimal valor = itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            vendas.add(new VendaResponse(pedido.getId(), pedido.getDataCriacao(), pedido.getStatus(),
                    pedido.getComprador().getNome(), valor,
                    itens.stream().map(ItemPedidoResponse::de).toList()));
        });
        return vendas;
    }
}
