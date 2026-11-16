package br.edu.atitus.camisa10.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnoECorTest {

    @ParameterizedTest
    @CsvSource(value = {
            "Camisa Internacional 26/27, 2026",
            "Camisa Retrô Brasil 2002, 2002",
            "Camisa Manga Longa Retrô Napoli 98/99, 1998",
            "Camisa Retrô Internacional 00/01, 2000",
            "#1 Brasil 26/27 Copa do Mundo, 2026",
            "Camisa Retrô Boca Juniors, NULO",
    }, nullValues = "NULO")
    void anoSaiDoNomeDaCamisa(String nome, Integer esperado) {
        assertThat(AnoCamisa.extrair(nome)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({
            "#FFFFFF, Branco",
            "#D52B1E, Vermelho",   // vermelho do River → família Vermelho
            "#FFDC02, Amarelo",
            "#0B3D91, Azul",       // azul do Boca
            "#9BB7E8, Azul-claro", // azul do Marseille
            "#1C2541, Preto",      // azul-marinho bem escuro fica mais perto do preto
    })
    void corQualquerCaiNaCorMaisProximaDaPaleta(String hex, String familia) {
        assertThat(CoresCamisa.familia(hex)).isEqualTo(familia);
    }
}
