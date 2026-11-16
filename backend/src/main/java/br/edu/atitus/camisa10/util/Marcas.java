package br.edu.atitus.camisa10.util;

import java.util.List;

/** Marcas (fornecedores de material esportivo) aceitas no cadastro de camisas. */
public final class Marcas {

    public static final List<String> TODAS = List.of(
            "Adidas", "Nike", "Puma", "New Balance", "Umbro", "Kappa", "Castore", "Topper",
            "Reebok", "EA7", "Le Coq Sportif", "Mizuno", "Joma", "Macron", "Hummel", "Volt", "Outra");

    private Marcas() {
    }
}
