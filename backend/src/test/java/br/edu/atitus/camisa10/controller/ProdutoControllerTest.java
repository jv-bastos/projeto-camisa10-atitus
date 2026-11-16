package br.edu.atitus.camisa10.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.atitus.camisa10.ApiTestBase;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;

/**
 * CRUD de produtos (RF04, RF05, RF08), catálogo (RF09, RF10) e RF15.
 * @Transactional desfaz as alterações no banco ao fim de cada teste.
 * As imagens de teste vão para target/test-uploads.
 */
@SpringBootTest(properties = "camisa10.upload.dir=target/test-uploads")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ProdutoControllerTest extends ApiTestBase {

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void compradorRecebe403AoCriarProdutoMesmoComDadosValidos() throws Exception {
        long produtosAntes = produtoRepository.count();

        mvc.perform(post("/api/produtos").header("Authorization", bearer(COMPRADOR))
                        .contentType(MediaType.APPLICATION_JSON).content(produtoJson("Camisa Pirata", "199.90")))
                .andExpect(status().isForbidden());

        assertThat(produtoRepository.count()).isEqualTo(produtosAntes);
    }

    @Test
    void vendedorCriaEditaEVeOProdutoNaSuaLista() throws Exception {
        String vendedor = bearer(VENDEDOR);

        long id = json(mvc.perform(post("/api/produtos").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(produtoJson("Camisa Teste", "199.90")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.corPrincipal").value("#AABBCC")) // normalizada para maiúsculas
                .andExpect(jsonPath("$.categorias[*].nome", containsInAnyOrder("Retrô", "Seleções")))
                // grade completa na ordem P, M, G, GG, XGG; tamanhos não informados ficam com 0
                .andExpect(jsonPath("$.estoque").value(3))
                .andExpect(jsonPath("$.tamanhos[*].tamanho", contains("P", "M", "G", "GG", "XGG")))
                .andExpect(jsonPath("$.tamanhos[*].quantidade", contains(1, 2, 0, 0, 0)))
                .andExpect(jsonPath("$.ano").value(1994))
                .andExpect(jsonPath("$.marca").value("Umbro"))
                .andExpect(jsonPath("$.corFamilia").value("Azul-claro"))) // #AABBCC fica mais perto do azul-claro
                .get("id").asLong();

        mvc.perform(put("/api/produtos/" + id).header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(produtoJson("Camisa Teste Editada", "149.90")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Camisa Teste Editada"))
                .andExpect(jsonPath("$.preco").value(149.90));

        mvc.perform(get("/api/vendedor/produtos").header("Authorization", vendedor))
                .andExpect(jsonPath("$[*].nome", hasItem("Camisa Teste Editada")));
    }

    @Test
    void produtoComDadosInvalidosRetornaErrosPorCampo() throws Exception {
        mvc.perform(post("/api/produtos").header("Authorization", bearer(VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "", "preco": 0, "estoques": { "P": -1, "M": 2 }, "categoriaIds": [],
                                  "corPrincipal": "amarelo", "corTexto": "#000000" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("Informe o nome do produto."))
                .andExpect(jsonPath("$.campos.preco").value("O preço deve ser maior que zero."))
                .andExpect(jsonPath("$.campos['estoques[P]']").value("O estoque não pode ser negativo."))
                .andExpect(jsonPath("$.campos.corPrincipal").value("A cor principal deve estar no formato #RRGGBB."))
                .andExpect(jsonPath("$.campos.categoriaIds").value("Escolha pelo menos uma categoria."))
                .andExpect(jsonPath("$.campos.ano").value("Informe o ano da camisa."));
    }

    @Test
    void marcaForaDaListaRetorna400() throws Exception {
        mvc.perform(post("/api/produtos").header("Authorization", bearer(VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Camisa X", "preco": 10, "ano": 2026, "marca": "Inventada", "estoques": { "M": 1 },
                                  "categoriaIds": [1], "corPrincipal": "#FFFFFF", "corTexto": "#000000" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Marca inválida. Escolha uma das opções da lista."));
    }

    @Test
    void categoriaInexistenteRetorna400() throws Exception {
        mvc.perform(post("/api/produtos").header("Authorization", bearer(VENDEDOR))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Camisa X", "preco": 10, "ano": 2026, "estoques": { "M": 1 }, "categoriaIds": [1, 9999],
                                  "corPrincipal": "#FFFFFF", "corTexto": "#000000" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Categoria informada não existe."));
    }

    @Test
    void vendedorNaoPodeEditarNemExcluirProdutoDeOutroVendedor() throws Exception {
        String outroVendedor = cadastrarOutroVendedor();
        Long produtoDoSeed = produtoRepository.findAll().get(0).getId();

        mvc.perform(put("/api/produtos/" + produtoDoSeed).header("Authorization", outroVendedor)
                        .contentType(MediaType.APPLICATION_JSON).content(produtoJson("Roubada", "1.00")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Você só pode alterar os produtos que você cadastrou."));
        mvc.perform(delete("/api/produtos/" + produtoDoSeed).header("Authorization", outroVendedor))
                .andExpect(status().isForbidden());

        // e a lista dele não mostra produtos de terceiros
        mvc.perform(get("/api/vendedor/produtos").header("Authorization", outroVendedor))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void exclusaoEhLogicaProdutoSomeDoCatalogoMasContinuaNoBanco() throws Exception {
        Produto produto = produtoRepository.findAll().get(0);

        mvc.perform(delete("/api/produtos/" + produto.getId()).header("Authorization", bearer(VENDEDOR)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/produtos/" + produto.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Produto não encontrado."));
        mvc.perform(get("/api/produtos"))
                .andExpect(jsonPath("$[*].id", not(hasItem(produto.getId().intValue()))));
        assertThat(produtoRepository.findById(produto.getId())).get()
                .extracting(Produto::isAtivo).isEqualTo(false);
    }

    @Test
    void catalogoFiltraPorCategoriaEBuscaPorNome() throws Exception {
        Long selecoes = categoriaRepository.findByNome("Seleções").orElseThrow().getId();

        // camisas com várias categorias aparecem em cada uma delas (ex.: Retrô Brasil 2002 em Seleções)
        JsonNode selecionadas = json(mvc.perform(get("/api/produtos").param("categoriaId", selecoes.toString()))
                .andExpect(status().isOk()));
        assertThat(selecionadas).hasSize(8);
        assertThat(selecionadas).allSatisfy(p -> assertThat(p.get("categorias").findValuesAsText("nome")).contains("Seleções"));
        assertThat(selecionadas.findValuesAsText("nome")).contains("Camisa Retrô Brasil 2002", "Camisa Seleção Brasileira 26/27");

        mvc.perform(get("/api/produtos").param("busca", "barcelona"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Camisa Barcelona 26/27"));

        // a busca ignora acentos e maiúsculas
        mvc.perform(get("/api/produtos").param("busca", "MONACO"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Camisa Mônaco 26/27"));
        mvc.perform(get("/api/produtos").param("busca", "selecao bras"))
                .andExpect(jsonPath("$[0].nome").value("Camisa Seleção Brasileira 26/27"));

        mvc.perform(get("/api/produtos"))
                .andExpect(jsonPath("$", hasSize(41)));
    }

    @Test
    void uploadDeImagemSalvaEServeOArquivo() throws Exception {
        Long id = produtoRepository.findAll().get(0).getId();
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 1, 2, 3};
        MockMultipartFile imagem = new MockMultipartFile("imagem", "foto.png", "image/png", png);

        String url = json(mvc.perform(multipart("/api/produtos/" + id + "/imagem").file(imagem)
                        .header("Authorization", bearer(VENDEDOR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagemUrl", startsWith("/uploads/")))
                .andExpect(jsonPath("$.imagemUrl", endsWith(".png"))))
                .get("imagemUrl").asText();

        // a imagem fica acessível publicamente
        byte[] baixado = mvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(baixado).isEqualTo(png);
    }

    @Test
    void uploadDeArquivoQueNaoEhImagemEhRejeitado() throws Exception {
        Long id = produtoRepository.findAll().get(0).getId();
        MockMultipartFile texto = new MockMultipartFile("imagem", "virus.txt", "text/plain", "oi".getBytes());

        mvc.perform(multipart("/api/produtos/" + id + "/imagem").file(texto)
                        .header("Authorization", bearer(VENDEDOR)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Formato de imagem não suportado. Use JPG, PNG ou WEBP."));
    }

    @Test
    void configuracaoDaPersonalizacaoEhPublica() throws Exception {
        mvc.perform(get("/api/config/personalizacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorAcrescimo").value(25.00))
                .andExpect(jsonPath("$.maxCaracteresNome").value(12));
    }

    @Test
    void catalogoDeFotosFiltraPorBuscaECategoriaESoParaVendedor() throws Exception {
        String vendedor = bearer(VENDEDOR);
        Long brasileirao = categoriaRepository.findByNome("Brasileirão").orElseThrow().getId();

        JsonNode inter = json(mvc.perform(get("/api/vendedor/fotos-catalogo").header("Authorization", vendedor)
                        .param("busca", "internacional 1997"))
                .andExpect(status().isOk()));
        assertThat(inter).hasSize(1);
        JsonNode primeira = inter.get(0);
        assertThat(primeira.get("nome").asText()).isEqualTo("Camisa Retrô Internacional 1997");
        assertThat(primeira.get("categorias").findValuesAsText("nome")).containsExactly("Brasileirão", "Retrô");
        assertThat(primeira.get("miniaturaUrl").asText()).startsWith("/catalogo-fotos/miniaturas/");
        assertThat(primeira.get("ano").asInt()).isEqualTo(1997); // sugerido pelo nome

        JsonNode doBrasileirao = json(mvc.perform(get("/api/vendedor/fotos-catalogo").header("Authorization", vendedor)
                .param("categoriaId", brasileirao.toString())));
        assertThat(doBrasileirao.size()).isGreaterThan(300); // 221 atuais + retrôs + manga longa
        assertThat(doBrasileirao).allSatisfy(f -> assertThat(f.get("categorias").findValuesAsText("nome")).contains("Brasileirão"));

        // a busca olha só o nome da camisa: "brasil" não traz os times da pasta "Brasileirão"
        JsonNode brasil = json(mvc.perform(get("/api/vendedor/fotos-catalogo").header("Authorization", vendedor)
                .param("busca", "brasil")));
        assertThat(brasil).isNotEmpty();
        brasil.forEach(f -> assertThat(f.get("nome").asText().toLowerCase()).contains("brasil"));
        assertThat(brasil.findValuesAsText("arquivo")).noneMatch(a -> a.contains("flamengo") || a.contains("corinthians"));

        // a miniatura é servida como arquivo estático
        mvc.perform(get(primeira.get("miniaturaUrl").asText())).andExpect(status().isOk());

        mvc.perform(get("/api/vendedor/fotos-catalogo").header("Authorization", bearer(COMPRADOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorUsaFotoDoCatalogoNoProduto() throws Exception {
        String vendedor = bearer(VENDEDOR);
        long id = json(mvc.perform(post("/api/produtos").header("Authorization", vendedor)
                .contentType(MediaType.APPLICATION_JSON).content(produtoJson("Camisa Do Catálogo", "99.90"))))
                .get("id").asLong();
        String arquivo = json(mvc.perform(get("/api/vendedor/fotos-catalogo").header("Authorization", vendedor)
                .param("busca", "internacional 1997"))).get(0).get("arquivo").asText();

        String imagemUrl = json(mvc.perform(post("/api/produtos/" + id + "/imagem-catalogo").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"arquivo\": \"%s\" }".formatted(arquivo)))
                .andExpect(status().isOk()))
                .get("imagemUrl").asText();
        assertThat(imagemUrl).startsWith("/uploads/");
        mvc.perform(get(imagemUrl)).andExpect(status().isOk());

        // só arquivos que estão no catálogo são aceitos (nada de "../")
        mvc.perform(post("/api/produtos/" + id + "/imagem-catalogo").header("Authorization", vendedor)
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"arquivo\": \"../../pom.xml\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Foto do catálogo não encontrada."));
    }

    // ---------------------------------------------------------------- auxiliares

    private String produtoJson(String nome, String preco) {
        Long retro = categoriaRepository.findByNome("Retrô").orElseThrow().getId();
        Long selecoes = categoriaRepository.findByNome("Seleções").orElseThrow().getId();
        return """
                { "nome": "%s", "descricao": "Teste", "preco": %s, "ano": 1994, "marca": "Umbro", "estoques": { "P": 1, "M": 2 },
                  "categoriaIds": [%d, %d], "corPrincipal": "#aabbcc", "corTexto": "#FFFFFF" }
                """.formatted(nome, preco, retro, selecoes);
    }

    private String cadastrarOutroVendedor() throws Exception {
        String json = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "nome": "Loja Rival", "email": "rival@camisa10.com",
                                  "senha": "123456", "perfil": "VENDEDOR" }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(json).get("token").asText();
    }
}
