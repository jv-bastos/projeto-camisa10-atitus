package br.edu.atitus.camisa10.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.edu.atitus.camisa10.dto.pedido.PersonalizacaoRequest;
import br.edu.atitus.camisa10.exception.RegraNegocioException;
import br.edu.atitus.camisa10.model.Personalizacao;

/**
 * Teste unitário (sem Spring) das regras de nome e número da personalização (RF11).
 */
class PersonalizacaoServiceTest {

    private final PersonalizacaoService service = new PersonalizacaoService(new BigDecimal("25.00"), 12);

    @Test
    void semPersonalizacaoRetornaNull() {
        assertThat(service.validar(null)).isNull();
    }

    @Test
    void nomeEhConvertidoParaMaiusculasEEspacosExtrasRemovidos() {
        Personalizacao p = service.validar(new PersonalizacaoRequest("  joão   pedro ", 7));

        assertThat(p.getNome()).isEqualTo("JOÃO PEDRO");
        assertThat(p.getNumero()).isEqualTo(7);
    }

    @Test
    void nomeCom12CaracteresEhAceito() {
        assertThat(service.validar(new PersonalizacaoRequest("ABCDEFGHIJKL", 10)).getNome()).hasSize(12);
    }

    @Test
    void nomeCom13CaracteresEhRejeitado() {
        assertThatThrownBy(() -> service.validar(new PersonalizacaoRequest("ABCDEFGHIJKLM", 10)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O nome da personalização deve ter no máximo 12 caracteres.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"PELE10", "NEYMAR JR.", "R9!", "CR-7", "ZICO_"})
    void nomeComCaracteresQueNaoSaoLetrasEhRejeitado(String nome) {
        assertThatThrownBy(() -> service.validar(new PersonalizacaoRequest(nome, 10)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O nome da personalização deve conter apenas letras e espaços.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void nomeVazioEhRejeitado(String nome) {
        assertThatThrownBy(() -> service.validar(new PersonalizacaoRequest(nome, 10)))
                .hasMessage("Informe o nome da personalização.");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 99})
    void numerosDe0a99SaoAceitos(int numero) {
        assertThat(service.validar(new PersonalizacaoRequest("ZICO", numero)).getNumero()).isEqualTo(numero);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 100, 999})
    void numerosForaDaFaixaSaoRejeitados(int numero) {
        assertThatThrownBy(() -> service.validar(new PersonalizacaoRequest("ZICO", numero)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O número da personalização deve estar entre 0 e 99.");
    }

    @Test
    void numeroAusenteEhRejeitado() {
        assertThatThrownBy(() -> service.validar(new PersonalizacaoRequest("ZICO", null)))
                .hasMessage("Informe o número da personalização.");
    }

    @Test
    void acrescimoVemDaConfiguracao() {
        assertThat(service.getValorAcrescimo()).isEqualByComparingTo("25.00");
    }
}
