package br.edu.atitus.camisa10.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.atitus.camisa10.model.ItemPedido;

/**
 * As consultas de vendas do vendedor (RF06 e RF07) serão adicionadas na Fase 3.
 */
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
