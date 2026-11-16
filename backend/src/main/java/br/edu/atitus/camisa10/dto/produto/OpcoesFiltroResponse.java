package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Opções para montar o painel de filtros da vitrine, a partir das camisas à venda:
 * só aparecem as marcas e cores que existem, e as faixas de preço e ano servem de exemplo.
 */
public record OpcoesFiltroResponse(
        List<String> marcas,
        List<String> cores,
        List<String> tamanhos,
        BigDecimal precoMin,
        BigDecimal precoMax,
        Integer anoMin,
        Integer anoMax,
        List<OpcaoOrdenacao> ordenacoes) {

    /** Valor enviado em ?ordenar= e o texto mostrado no app. */
    public record OpcaoOrdenacao(String valor, String rotulo) {
    }
}
