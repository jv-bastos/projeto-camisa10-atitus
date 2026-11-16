package br.edu.atitus.camisa10.dto.produto;

import java.util.List;

/**
 * Foto do catálogo que o vendedor pode usar ao cadastrar uma camisa.
 * - arquivo: identificador da foto (enviar de volta para usá-la num produto);
 * - nome, ano e categorias: sugestões para preencher o cadastro (ano pode ser null);
 * - miniaturaUrl / fotoUrl: caminhos relativos, como em imagemUrl.
 */
public record FotoCatalogoResponse(
        String arquivo,
        String nome,
        String pasta,
        Integer ano,
        List<CategoriaResponse> categorias,
        String miniaturaUrl,
        String fotoUrl) {
}
