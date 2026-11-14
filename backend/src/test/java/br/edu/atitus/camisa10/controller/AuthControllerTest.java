package br.edu.atitus.camisa10.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Testes de cadastro (RF01) e login (RF02). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void cadastroDeveRetornarTokenEUsuarioSemSenha() throws Exception {
        postar("/api/auth/registro", """
                { "nome": "Novo Boleiro", "email": "  Novo@Email.com ",
                  "senha": "segredo1", "perfil": "COMPRADOR" }
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value("novo@email.com"))
                .andExpect(jsonPath("$.usuario.perfil").value("COMPRADOR"))
                .andExpect(jsonPath("$.usuario.senha").doesNotExist())
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());
    }

    @Test
    void cadastroComEmailRepetidoDeveRetornar409() throws Exception {
        postar("/api/auth/registro", """
                { "nome": "Outro", "email": "VENDEDOR@camisa10.com",
                  "senha": "123456", "perfil": "VENDEDOR" }
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Este e-mail já está cadastrado."));
    }

    @Test
    void cadastroComCamposInvalidosDeveListarOsErros() throws Exception {
        postar("/api/auth/registro", """
                { "nome": "", "email": "nao-e-email", "senha": "123" }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").value("Informe o nome."))
                .andExpect(jsonPath("$.campos.email").value("Informe um e-mail válido."))
                .andExpect(jsonPath("$.campos.senha").value("A senha deve ter entre 6 e 72 caracteres."))
                .andExpect(jsonPath("$.campos.perfil").exists());
    }

    @Test
    void cadastroComPerfilInexistenteDeveRetornar400() throws Exception {
        postar("/api/auth/registro", """
                { "nome": "Hacker", "email": "h@h.com", "senha": "123456", "perfil": "ADMIN" }
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Valor inválido para o campo 'perfil'."));
    }

    @Test
    void loginValidoDeveRetornarTokenEPerfil() throws Exception {
        postar("/api/auth/login", """
                { "email": "vendedor@camisa10.com", "senha": "123456" }
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.perfil").value("VENDEDOR"));
    }

    @Test
    void loginComSenhaErradaDeveRetornar401() throws Exception {
        postar("/api/auth/login", """
                { "email": "vendedor@camisa10.com", "senha": "errada" }
                """)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
    }

    private ResultActions postar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
