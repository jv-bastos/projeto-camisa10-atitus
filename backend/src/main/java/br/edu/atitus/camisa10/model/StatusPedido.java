package br.edu.atitus.camisa10.model;

/**
 * Situação de um pedido. Como o pagamento é simulado, todo pedido
 * já nasce CONCLUIDO.
 */
public enum StatusPedido {
    CONCLUIDO,
    ENVIADO,
    ENTREGUE
}
