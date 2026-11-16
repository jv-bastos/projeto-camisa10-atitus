package br.edu.atitus.camisa10.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Prepara textos para a busca do catálogo: tira acentos, deixa minúsculo e remove
 * espaços das pontas. Assim "sao paulo", "SÃO PAULO" e "São Paulo" encontram o mesmo produto.
 */
public final class TextoBusca {

    private TextoBusca() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcentos.toLowerCase(Locale.ROOT).trim();
    }
}
