package br.edu.atitus.camisa10.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.atitus.camisa10.ApiTestBase;
import br.edu.atitus.camisa10.repository.ProdutoRepository;

/** Vitrine: filtros (preço, ano, cor, marca, tamanho) e ordenações de GET /api/produtos. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class CatalogoFiltrosTest extends ApiTestBase {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void ordenaPorPrecoNasDuasDirecoes() throws Exception {
        List<BigDecimal> crescente = valores(listar("ordenar=preco"), "preco");
        assertThat(crescente).isSortedAccordingTo(BigDecimal::compareTo);
        List<BigDecimal> decrescente = valores(listar("ordenar=preco_desc"), "preco");
        assertThat(decrescente).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    @Test
    void filtraPorFaixaDePrecoEIntervaloDeAnos() throws Exception {
        assertThat(valores(listar("precoMin=300&precoMax=350"), "preco"))
                .isNotEmpty()
                .allSatisfy(p -> assertThat(p).isBetween(new BigDecimal("300"), new BigDecimal("350")));

        assertThat(nomes(listar("anoMin=2000&anoMax=2005")))
                .containsExactlyInAnyOrder("Camisa Retrô Brasil 2002", "Camisa Retrô Corinthians 2000",
                        "Camisa Retrô Real Madrid 2003");
    }

    @Test
    void ordenaPorAnoComAsMaisNovasPrimeiro() throws Exception {
        JsonNode lista = listar("ordenar=ano_desc");
        List<Integer> anos = new ArrayList<>();
        lista.forEach(p -> anos.add(p.get("ano").asInt()));
        assertThat(anos).isSortedAccordingTo((a, b) -> b - a);
        assertThat(anos.get(anos.size() - 1)).isEqualTo(1980); // Retrô Holanda 1980 é a mais antiga
    }

    @Test
    void filtraPorCorMarcaETamanho() throws Exception {
        JsonNode amarelas = listar("cores=Amarelo");
        assertThat(nomes(amarelas)).contains("Camisa Seleção Brasileira 26/27", "Camisa Retrô Brasil 2002");
        assertThat(amarelas.findValuesAsText("corFamilia")).containsOnly("Amarelo");

        assertThat(nomes(listar("marcas=Topper"))).containsExactly("Camisa Retrô Corinthians 2000");
        assertThat(listar("marcas=Topper,Reebok")).hasSize(2);

        // a Holanda 1980 não tem o tamanho P em estoque
        assertThat(nomes(listar("tamanho=P"))).doesNotContain("Camisa Retrô Holanda 1980");
        assertThat(nomes(listar("tamanho=M"))).contains("Camisa Retrô Holanda 1980");
    }

    @Test
    void maisVendidasELancamentos() throws Exception {
        Long napoli = produtoRepository.findAll().stream().filter(p -> p.getNome().equals("Camisa Napoli 26/27"))
                .findFirst().orElseThrow().getId();
        mvc.perform(post("/api/pedidos").header("Authorization", bearer(COMPRADOR)).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"itens\": [ { \"produtoId\": %d, \"tamanho\": \"M\", \"quantidade\": 2 } ] }".formatted(napoli)))
                .andExpect(status().isCreated());

        assertThat(listar("ordenar=vendidas").get(0).get("nome").asText()).isEqualTo("Camisa Napoli 26/27");
        // lançamentos = cadastradas por último; a última do seed é a Manga Longa Retrô Napoli 98/99
        assertThat(listar("ordenar=lancamentos").get(0).get("nome").asText())
                .isEqualTo("Camisa Manga Longa Retrô Napoli 98/99");
    }

    @Test
    void ordenacaoInvalidaRetorna400() throws Exception {
        mvc.perform(get("/api/produtos?ordenar=qualquer"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Ordenação inválida: qualquer."));
    }

    @Test
    void opcoesDoPainelDeFiltros() throws Exception {
        JsonNode opcoes = json(mvc.perform(get("/api/produtos/filtros")).andExpect(status().isOk()));
        List<String> marcas = new ArrayList<>();
        opcoes.get("marcas").forEach(m -> marcas.add(m.asText()));
        assertThat(marcas).contains("Adidas", "Nike", "Topper").startsWith("Adidas"); // na ordem da lista de marcas
        assertThat(opcoes.get("anoMin").asInt()).isEqualTo(1980);
        assertThat(opcoes.get("anoMax").asInt()).isEqualTo(2026);
        assertThat(opcoes.get("tamanhos")).hasSize(5);
        assertThat(opcoes.get("ordenacoes")).hasSize(8);
    }

    // ---------------------------------------------------------------- auxiliares

    private JsonNode listar(String consulta) throws Exception {
        return json(mvc.perform(get("/api/produtos?" + consulta)).andExpect(status().isOk()));
    }

    /** Só o nome de cada produto (sem os nomes das categorias dentro dele). */
    private List<String> nomes(JsonNode lista) {
        List<String> nomes = new ArrayList<>();
        lista.forEach(p -> nomes.add(p.get("nome").asText()));
        return nomes;
    }

    private List<BigDecimal> valores(JsonNode lista, String campo) {
        List<BigDecimal> valores = new ArrayList<>();
        lista.forEach(p -> valores.add(p.get(campo).decimalValue()));
        return valores;
    }
}
