package br.edu.atitus.camisa10.dto.produto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

        @NotNull(message = "Informe o estoque.")
        @Min(value = 0, message = "O estoque não pode ser negativo.")
        Integer estoque,

        @NotNull(message = "Escolha a categoria.")
        Long categoriaId,

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
        corPrincipal = corPrincipal == null ? null : corPrincipal.trim().toUpperCase();
        corTexto = corTexto == null ? null : corTexto.trim().toUpperCase();
    }
}
