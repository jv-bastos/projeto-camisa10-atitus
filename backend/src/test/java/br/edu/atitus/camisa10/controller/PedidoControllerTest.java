package br.edu.atitus.camisa10.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.atitus.camisa10.ApiTestBase;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.repository.PedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;

/**
 * Regras críticas da compra (RF11 a RF14).
 *
 * ATENÇÃO: esta classe NÃO usa @Transactional de propósito. Assim cada pedido
 * roda na transação real do PedidoService, e conseguimos provar que, quando
 * algo falha, nada é gravado (nem pedido, nem baixa de estoque).
 * Por isso os testes comparam valores "antes x depois" em vez de valores fixos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PedidoControllerTest extends ApiTestBase {

    private static final BigDecimal ACRESCIMO = new BigDecimal("25.00");

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    void totalDeveIncluirOAcrescimoDeCadaCamisaPersonalizada() throws Exception {
        Produto selecao = produto("Seleção Brasileira");
        int estoqueAntes = estoque(selecao);

        // 2 camisas personalizadas + 1 sem personalização
        JsonNode pedido = json(comprar("""
                { "itens": [
                    { "produtoId": %d, "quantidade": 2, "personalizacao": { "nome": "ronaldinho", "numero": 10 } },
                    { "produtoId": %d, "quantidade": 1 } ] }
                """.formatted(selecao.getId(), selecao.getId()))
                .andExpect(status().isCreated()));

        BigDecimal esperado = selecao.getPreco().add(ACRESCIMO).multiply(BigDecimal.valueOf(2))
                .add(selecao.getPreco());
        assertThat(pedido.get("valorTotal").decimalValue()).isEqualByComparingTo(esperado);
        assertThat(pedido.get("status").asText()).isEqualTo("CONCLUIDO");
        assertThat(pedido.at("/itens/0/personalizacao/nome").asText()).isEqualTo("RONALDINHO");
        assertThat(pedido.at("/itens/0/valorPersonalizacao").decimalValue()).isEqualByComparingTo(ACRESCIMO);
        assertThat(pedido.at("/itens/1/personalizacao").isNull()).isTrue();
        assertThat(pedido.at("/itens/1/valorPersonalizacao").decimalValue()).isEqualByComparingTo("0");
        // estoque baixado
        assertThat(estoque(selecao)).isEqualTo(estoqueAntes - 3);
    }

    @Test
    void nomeCom13CaracteresDeveSerRejeitadoSemGravarNada() throws Exception {
        Produto gremio = produto("Grêmio");
        long pedidosAntes = pedidoRepository.count();
        int estoqueAntes = estoque(gremio);

        comprar(umItem(gremio.getId(), 1, "ABCDEFGHIJKLM", 10))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O nome da personalização deve ter no máximo 12 caracteres."));

        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes);
        assertThat(estoque(gremio)).isEqualTo(estoqueAntes);
    }

    @Test
    void numero100DeveSerRejeitado() throws Exception {
        long pedidosAntes = pedidoRepository.count();

        comprar(umItem(produto("Grêmio").getId(), 1, "RENATO", 100))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O número da personalização deve estar entre 0 e 99."));

        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes);
    }

    @Test
    void comprarMaisQueOEstoqueDeveRetornarErroENaoCriarPedido() throws Exception {
        Produto holanda = produto("Holanda");
        long pedidosAntes = pedidoRepository.count();
        int estoqueAntes = estoque(holanda);

        comprar(umItem(holanda.getId(), estoqueAntes + 1, null, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "Estoque insuficiente para \"Camisa Retrô Holanda 1980\". Disponível: " + estoqueAntes + "."));

        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes);
        assertThat(estoque(holanda)).isEqualTo(estoqueAntes);
    }

    @Test
    void compraEhTransacionalSeUmItemFalharNadaEhGravado() throws Exception {
        Produto gremio = produto("Grêmio");
        Produto holanda = produto("Holanda");
        long pedidosAntes = pedidoRepository.count();
        int estoqueGremioAntes = estoque(gremio);
        int estoqueHolandaAntes = estoque(holanda);

        // 1º item válido (baixaria o estoque do Grêmio), 2º item sem estoque
        comprar("""
                { "itens": [
                    { "produtoId": %d, "quantidade": 1 },
                    { "produtoId": %d, "quantidade": %d } ] }
                """.formatted(gremio.getId(), holanda.getId(), estoqueHolandaAntes + 1))
                .andExpect(status().isBadRequest());

        // rollback: o estoque do 1º item voltou ao valor original
        assertThat(estoque(gremio)).isEqualTo(estoqueGremioAntes);
        assertThat(estoque(holanda)).isEqualTo(estoqueHolandaAntes);
        assertThat(pedidoRepository.count()).isEqualTo(pedidosAntes);
    }

    @Test
    void mesmoProdutoEmDuasLinhasSomaAsQuantidadesNoEstoque() throws Exception {
        Produto holanda = produto("Holanda");
        int estoqueAntes = estoque(holanda);

        // linha 1 leva todo o estoque, linha 2 (personalizada) pede mais 1
        comprar("""
                { "itens": [
                    { "produtoId": %d, "quantidade": %d },
                    { "produtoId": %d, "quantidade": 1, "personalizacao": { "nome": "GULLIT", "numero": 10 } } ] }
                """.formatted(holanda.getId(), estoqueAntes, holanda.getId()))
                .andExpect(status().isBadRequest());

        assertThat(estoque(holanda)).isEqualTo(estoqueAntes);
    }

    @Test
    void pedidoSemItensDeveRetornar400() throws Exception {
        comprar("{ \"itens\": [] }")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.itens").value("Adicione pelo menos um item ao pedido."));
    }

    @Test
    void compraApareceNoHistoricoDoCompradorENasVendasDoVendedorComPersonalizacao() throws Exception {
        Produto barcelona = produto("Barcelona");
        String vendedor = bearer(VENDEDOR);
        JsonNode resumoAntes = json(mvc.perform(get("/api/vendedor/resumo").header("Authorization", vendedor)));

        long pedidoId = json(comprar(umItem(barcelona.getId(), 1, "Messi", 10))
                .andExpect(status().isCreated())).get("id").asLong();

        // histórico do comprador (RF14)
        mvc.perform(get("/api/pedidos/meus").header("Authorization", bearer(COMPRADOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)].itens[0].personalizacao.nome", pedidoId).value(contains("MESSI")))
                .andExpect(jsonPath("$[?(@.id == %d)].itens[0].personalizacao.numero", pedidoId).value(contains(10)));

        // histórico de vendas do vendedor dono do produto (RF07)
        mvc.perform(get("/api/vendedor/vendas").header("Authorization", vendedor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.pedidoId == %d)].compradorNome", pedidoId).value(contains("Comprador Teste")))
                .andExpect(jsonPath("$[?(@.pedidoId == %d)].itens[0].personalizacao.nome", pedidoId).value(contains("MESSI")))
                .andExpect(jsonPath("$[?(@.pedidoId == %d)].itens[0].personalizacao.numero", pedidoId).value(contains(10)));

        // dashboard (RF06): +1 venda e faturamento + (preço + acréscimo)
        JsonNode resumoDepois = json(mvc.perform(get("/api/vendedor/resumo").header("Authorization", vendedor)));
        assertThat(resumoDepois.get("numeroVendas").asLong()).isEqualTo(resumoAntes.get("numeroVendas").asLong() + 1);
        assertThat(resumoDepois.get("faturamento").decimalValue())
                .isEqualByComparingTo(resumoAntes.get("faturamento").decimalValue()
                        .add(barcelona.getPreco()).add(ACRESCIMO));
    }

    // ---------------------------------------------------------------- auxiliares

    private ResultActions comprar(String json) throws Exception {
        return mvc.perform(post("/api/pedidos").header("Authorization", bearer(COMPRADOR))
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private String umItem(Long produtoId, int quantidade, String nome, Integer numero) {
        String personalizacao = nome == null ? ""
                : ", \"personalizacao\": { \"nome\": \"%s\", \"numero\": %d }".formatted(nome, numero);
        return "{ \"itens\": [ { \"produtoId\": %d, \"quantidade\": %d%s } ] }"
                .formatted(produtoId, quantidade, personalizacao);
    }

    private Produto produto(String parteDoNome) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome().contains(parteDoNome))
                .findFirst().orElseThrow();
    }

    /** Lê o estoque direto do banco (fora de transação, sempre o valor gravado). */
    private int estoque(Produto produto) {
        return produtoRepository.findById(produto.getId()).orElseThrow().getEstoque();
    }
}
