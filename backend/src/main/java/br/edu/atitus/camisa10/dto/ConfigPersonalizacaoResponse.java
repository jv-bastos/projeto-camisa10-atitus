package br.edu.atitus.camisa10.dto;

import java.math.BigDecimal;

/** Regras da personalização que o app precisa conhecer: { valorAcrescimo, maxCaracteresNome }. */
public record ConfigPersonalizacaoResponse(BigDecimal valorAcrescimo, int maxCaracteresNome) {
}
