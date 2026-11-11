package br.edu.atitus.camisa10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.atitus.camisa10.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** Histórico de compras do comprador, do mais recente para o mais antigo (RF14). */
    List<Pedido> findByCompradorIdOrderByDataCriacaoDesc(Long compradorId);
}
