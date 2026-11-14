package br.edu.atitus.camisa10.dto.auth;

import java.util.Locale;

/** E-mails são guardados e comparados sem espaços nas pontas e em minúsculas. */
final class EmailUtil {

    private EmailUtil() {
    }

    static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
