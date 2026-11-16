package br.edu.atitus.camisa10.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Paleta de cores da loja (a mesma do app) e a "família" de uma cor qualquer.
 * O filtro da vitrine usa a família: uma camisa #D52B1E entra em "Vermelho",
 * a cor da paleta mais próxima.
 */
public final class CoresCamisa {

    /** Nome → hexadecimal, na ordem em que aparecem no app. */
    public static final Map<String, String> PALETA;

    static {
        Map<String, String> paleta = new LinkedHashMap<>();
        paleta.put("Branco", "#FFFFFF");
        paleta.put("Preto", "#111111");
        paleta.put("Vermelho", "#C8102E");
        paleta.put("Azul", "#0A6EB4");
        paleta.put("Verde", "#006437");
        paleta.put("Amarelo", "#FFDC02");
        paleta.put("Laranja", "#F36C21");
        paleta.put("Azul-claro", "#75AADB");
        paleta.put("Grená", "#A50044");
        paleta.put("Roxo", "#6A3FB5");
        paleta.put("Cinza", "#5A5A5A");
        paleta.put("Rosa", "#F4A7C3");
        PALETA = Collections.unmodifiableMap(paleta);
    }

    public static final List<String> NOMES = List.copyOf(PALETA.keySet());

    private CoresCamisa() {
    }

    /** Nome da cor da paleta mais próxima (distância RGB). Hex inválido → null. */
    public static String familia(String hex) {
        int[] rgb = rgb(hex);
        if (rgb == null) {
            return null;
        }
        return PALETA.entrySet().stream()
                .min(Comparator.comparingInt(e -> distancia(rgb, rgb(e.getValue()))))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static int distancia(int[] a, int[] b) {
        int dr = a[0] - b[0];
        int dg = a[1] - b[1];
        int db = a[2] - b[2];
        return dr * dr + dg * dg + db * db;
    }

    private static int[] rgb(String hex) {
        if (hex == null || !hex.matches("^#[0-9A-Fa-f]{6}$")) {
            return null;
        }
        int valor = Integer.parseInt(hex.substring(1), 16);
        return new int[] { (valor >> 16) & 0xFF, (valor >> 8) & 0xFF, valor & 0xFF };
    }
}
