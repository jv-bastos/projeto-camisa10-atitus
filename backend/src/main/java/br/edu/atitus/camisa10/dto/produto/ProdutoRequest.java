package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import br.edu.atitus.camisa10.model.Tamanho;

/**
 * Dados para criar ou editar um produto (RF04).
 * A imagem é enviada separadamente em POST /api/produtos/{id}/imagem.
 */
public record ProdutoRequest(

        @NotBlank(message = "Informe o nome do produto.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
        String descricao,

        @NotNull(message = "Informe o preço.")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "Preço inválido (use no máximo 2 casas decimais).")
        BigDecimal preco,

        /** Unidades por tamanho, ex.: { "P": 2, "M": 5, "G": 5, "GG": 3, "XGG": 1 }. Tamanho ausente = 0. */
        @NotEmpty(message = "Informe o estoque de cada tamanho.")
        Map<Tamanho, @NotNull(message = "Informe a quantidade do tamanho.")
                     @Min(value = 0, message = "O estoque não pode ser negativo.") Integer> estoques,

        /** Ano da camisa: temporada 26/27 = 2026; retrô = ano do modelo. */
        @NotNull(message = "Informe o ano da camisa.")
        @Min(value = 1900, message = "Informe um ano entre 1900 e 2100.")
        @Max(value = 2100, message = "Informe um ano entre 1900 e 2100.")
        Integer ano,

        /** Marca do material esportivo (opcional), uma das listadas em Marcas.TODAS. */
        @Size(max = 40, message = "A marca deve ter no máximo 40 caracteres.")
        String marca,

        /** Uma ou mais categorias (ex.: Retrô e Seleções). */
        @NotEmpty(message = "Escolha pelo menos uma categoria.")
        List<@NotNull(message = "Categoria inválida.") Long> categoriaIds,

        @NotBlank(message = "Escolha a cor principal da camisa.")
        @Pattern(regexp = COR_HEX, message = "A cor principal deve estar no formato #RRGGBB.")
        String corPrincipal,

        @NotBlank(message = "Escolha a cor do texto.")
        @Pattern(regexp = COR_HEX, message = "A cor do texto deve estar no formato #RRGGBB.")
        String corTexto) {

    /** Cor hexadecimal, ex.: #FFDC02 */
    public static final String COR_HEX = "^#[0-9A-Fa-f]{6}$";

    /** Remove espaços extras e padroniza as cores em maiúsculas. */
    public ProdutoRequest {
        nome = nome == null ? null : nome.trim();
        descricao = descricao == null ? null : descricao.trim();
        marca = marca == null || marca.isBlank() ? null : marca.trim();
        corPrincipal = corPrincipal == null ? null : corPrincipal.trim().toUpperCase();
        corTexto = corTexto == null ? null : corTexto.trim().toUpperCase();
    }
}
