package br.edu.atitus.camisa10.dto.pedido;

import java.math.BigDecimal;

import br.edu.atitus.camisa10.model.ItemPedido;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;

/**
 * Item de um pedido, com as cores do produto para o app desenhar a
 * miniatura da personalização (CamisaPreview) nos históricos.
 */
public record ItemPedidoResponse(
        Long id,
        Long produtoId,
        String produtoNome,
        Tamanho tamanho,
        String imagemUrl,
        String corPrincipal,
        String corTexto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal valorPersonalizacao,
        BigDecimal subtotal,
        PersonalizacaoResponse personalizacao) {

    public static ItemPedidoResponse de(ItemPedido item) {
        Produto produto = item.getProduto();
        return new ItemPedidoResponse(item.getId(), produto.getId(), produto.getNome(), item.getTamanho(),
                produto.getImagemUrl(),
                produto.getCorPrincipal(), produto.getCorTexto(), item.getQuantidade(),
                item.getPrecoUnitario(), item.getValorPersonalizacao(), item.getSubtotal(),
                PersonalizacaoResponse.de(item.getPersonalizacao()));
    }
}
