package br.edu.atitus.camisa10.dto.vendedor;

import java.math.BigDecimal;

/** Números do dashboard do vendedor (RF06). */
public record ResumoVendedorResponse(long totalProdutos, long numeroVendas, BigDecimal faturamento) {
}
