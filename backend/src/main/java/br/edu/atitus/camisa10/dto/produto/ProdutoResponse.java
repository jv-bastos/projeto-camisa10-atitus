package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;

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
        String imagemUrl,
        String corPrincipal,
        String corTexto,
        CategoriaResponse categoria) {

    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getEstoque(),
                p.getImagemUrl(), p.getCorPrincipal(), p.getCorTexto(), CategoriaResponse.de(p.getCategoria()));
    }
}
