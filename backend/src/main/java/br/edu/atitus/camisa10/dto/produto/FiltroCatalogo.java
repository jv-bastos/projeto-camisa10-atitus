package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;
import java.util.List;

import br.edu.atitus.camisa10.model.Tamanho;

/**
 * Filtros e ordenação da vitrine (todos opcionais), vindos de GET /api/produtos.
 * - cores: famílias da paleta ("Branco", "Vermelho"...); marcas: "Nike", "Adidas"...
 * - tamanho: só camisas com estoque nesse tamanho
 * - ordenar: nome | nome_desc | preco | preco_desc | ano_desc | ano | vendidas | lancamentos
 */
public record FiltroCatalogo(
        Long categoriaId,
        String busca,
        BigDecimal precoMin,
        BigDecimal precoMax,
        Integer anoMin,
        Integer anoMax,
        List<String> cores,
        List<String> marcas,
        Tamanho tamanho,
        String ordenar) {

    public static FiltroCatalogo vazio() {
        return new FiltroCatalogo(null, null, null, null, null, null, null, null, null, null);
    }
}
