package br.edu.atitus.camisa10.dto.produto;

import jakarta.validation.constraints.NotBlank;

/** Foto do catálogo escolhida para um produto (o "arquivo" vem de GET /api/vendedor/fotos-catalogo). */
public record FotoCatalogoRequest(
        @NotBlank(message = "Escolha uma foto do catálogo.")
        String arquivo) {
}
