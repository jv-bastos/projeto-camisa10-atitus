package br.edu.atitus.camisa10.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TextoBuscaTest {

    @ParameterizedTest
    @CsvSource({
            "São Paulo, sao paulo",
            "  SELEÇÃO Brasileira  , selecao brasileira",
            "Mônaco, monaco",
            "Atlético de Madrid, atletico de madrid",
            "Camisa 10, camisa 10",
    })
    void tiraAcentosDeixaMinusculoERemoveEspacosDasPontas(String entrada, String esperado) {
        assertThat(TextoBusca.normalizar(entrada)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource(value = { "NULO", "''" }, nullValues = "NULO")
    void textoNuloOuVazioViraVazio(String entrada) {
        assertThat(TextoBusca.normalizar(entrada)).isEmpty();
    }
}
