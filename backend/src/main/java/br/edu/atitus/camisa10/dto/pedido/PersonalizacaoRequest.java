package br.edu.atitus.camisa10.dto.pedido;

/**
 * Nome e número escolhidos pelo comprador.
 * As regras (tamanho, caracteres, faixa do número) são verificadas no
 * PersonalizacaoService, em um só lugar.
 */
public record PersonalizacaoRequest(String nome, Integer numero) {
}
