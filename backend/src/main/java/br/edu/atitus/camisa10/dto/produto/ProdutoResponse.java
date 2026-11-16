package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;
import java.util.List;

import br.edu.atitus.camisa10.model.Produto;

/**
 * Produto devolvido pela API.
 * imagemUrl é um caminho relativo (ex.: "/uploads/abc.jpg"); o app junta com a URL da API.
 * Se for null, o app desenha a camisa com as cores do produto.
 */
public record ProdutoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        List<EstoqueTamanhoResponse> tamanhos,
        String imagemUrl,
        String corPrincipal,
        String corTexto,
        String corFamilia,
        Integer ano,
        String marca,
        List<CategoriaResponse> categorias) {

    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getEstoque(),
                EstoqueTamanhoResponse.grade(p),
                p.getImagemUrl(), p.getCorPrincipal(), p.getCorTexto(), p.getCorFamilia(), p.getAno(), p.getMarca(),
                p.getCategorias().stream().map(CategoriaResponse::de).toList());
    }
}
