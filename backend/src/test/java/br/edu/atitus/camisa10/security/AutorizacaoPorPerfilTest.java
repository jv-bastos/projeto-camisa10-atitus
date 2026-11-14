package br.edu.atitus.camisa10.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Garante que as regras de acesso por perfil são aplicadas no BACKEND.
 * Os tokens são obtidos pelo próprio endpoint de login, como o app faz.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AutorizacaoPorPerfilTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void semTokenRotaProtegidaDeveRetornar401() throws Exception {
        mvc.perform(get("/api/vendedor/resumo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value(
                        "Você precisa estar logado para acessar este recurso. Faça login novamente."));
    }

    @Test
    void tokenFalsificadoDeveRetornar401() throws Exception {
        mvc.perform(get("/api/pedidos/meus").header("Authorization", "Bearer token.falso.123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void compradorNaoPodeCriarEditarNemExcluirProduto() throws Exception {
        String token = "Bearer " + login("comprador@camisa10.com");

        // RF15: o bloqueio acontece no backend, não só na interface
        mvc.perform(post("/api/produtos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Seu perfil não tem permissão para realizar esta ação."));
        mvc.perform(put("/api/produtos/1").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/produtos/1").header("Authorization", token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/vendedor/vendas").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorNaoPodeComprar() throws Exception {
        String token = "Bearer " + login("vendedor@camisa10.com");

        mvc.perform(get("/api/pedidos/meus").header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorPassaPelaSegurancaNaRotaDeProdutos() throws Exception {
        String token = "Bearer " + login("vendedor@camisa10.com");

        int status = mvc.perform(post("/api/produtos").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andReturn().getResponse().getStatus();

        // a segurança deixa passar (o resultado final depende do controller da Fase 3)
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void rotaPublicaNaoExigeToken() throws Exception {
        int status = mvc.perform(get("/api/categorias")).andReturn().getResponse().getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    private String login(String email) throws Exception {
        String json = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }
}
