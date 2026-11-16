package br.edu.atitus.camisa10.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Descobre o ano de uma camisa pelo nome, para sugerir no cadastro:
 * "Retrô Brasil 2002" → 2002; "Internacional 26/27" → 2026; "Napoli 98/99" → 1998.
 * Temporadas com dois dígitos: 50 a 99 → 1900; 00 a 49 → 2000. Sem ano no nome → null.
 */
public final class AnoCamisa {

    private static final Pattern ANO_COMPLETO = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
    private static final Pattern TEMPORADA = Pattern.compile("\\b(\\d{2})/(\\d{2})\\b");

    private AnoCamisa() {
    }

    public static Integer extrair(String nome) {
        if (nome == null) {
            return null;
        }
        Matcher completo = ANO_COMPLETO.matcher(nome);
        if (completo.find()) {
            return Integer.parseInt(completo.group(1));
        }
        Matcher temporada = TEMPORADA.matcher(nome);
        if (temporada.find()) {
            int doisDigitos = Integer.parseInt(temporada.group(1));
            return doisDigitos >= 50 ? 1900 + doisDigitos : 2000 + doisDigitos;
        }
        return null;
    }
}
