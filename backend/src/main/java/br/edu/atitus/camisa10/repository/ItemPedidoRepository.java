package br.edu.atitus.camisa10.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.atitus.camisa10.model.ItemPedido;

/** Consultas de vendas do ponto de vista do vendedor (RF06 e RF07). */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    /** Todos os itens vendidos de produtos do vendedor, do pedido mais recente para o mais antigo. */
    @Query("""
            select i from ItemPedido i
              join fetch i.pedido p
              join fetch p.comprador
              join fetch i.produto pr
              left join fetch i.personalizacao
            where pr.vendedor.id = :vendedorId
            order by p.dataCriacao desc, p.id desc, i.id
            """)
    List<ItemPedido> buscarVendasDoVendedor(@Param("vendedorId") Long vendedorId);

    /** Número de pedidos diferentes que contêm produtos do vendedor. */
    @Query("select count(distinct i.pedido.id) from ItemPedido i where i.produto.vendedor.id = :vendedorId")
    long contarPedidosDoVendedor(@Param("vendedorId") Long vendedorId);

    /** Faturamento: soma de (preço + personalização) x quantidade. Retorna null se não houver vendas. */
    @Query("""
            select sum((i.precoUnitario + i.valorPersonalizacao) * i.quantidade)
            from ItemPedido i where i.produto.vendedor.id = :vendedorId
            """)
    BigDecimal somarFaturamentoDoVendedor(@Param("vendedorId") Long vendedorId);
}
