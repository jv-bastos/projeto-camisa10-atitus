package br.edu.atitus.camisa10.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Simula um banco antigo e confere as migrações automáticas.
 * @DirtiesContext descarta o banco alterado para não afetar os outros testes.
 */
@SpringBootTest
@ActiveProfiles("dev")
@DirtiesContext
class MigracoesBancoTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MigracoesBanco migracoes;

    @Test
    void categoriaUnicaEhCopiadaSemDuplicarEAColunaAntigaApagada() {
        assertThat(migracoes.colunaExiste("produtos", "categoria_id")).isFalse(); // banco novo: nada a migrar

        // formato antigo: Internacional com "Francês" (nova para ele) e Retrô Brasil 2002 com "Seleções" (que ele já tem)
        jdbc.execute("alter table produtos add column categoria_id bigint");
        jdbc.update("update produtos set categoria_id = (select id from categorias where nome = 'Francês') where nome = 'Camisa Internacional 26/27'");
        jdbc.update("update produtos set categoria_id = (select id from categorias where nome = 'Seleções') where nome = 'Camisa Retrô Brasil 2002'");

        migracoes.run(null);

        assertThat(migracoes.colunaExiste("produtos", "categoria_id")).isFalse();
        assertThat(categoriasDe("Camisa Internacional 26/27")).containsExactlyInAnyOrder("Brasileirão", "Francês");
        assertThat(categoriasDe("Camisa Retrô Brasil 2002")).containsExactlyInAnyOrder("Retrô", "Seleções");
    }

    @Test
    void produtoSemGradeRecebeOEstoqueAntigoNoTamanhoM() {
        // formato antigo: produto com estoque total e nenhuma linha por tamanho
        Long id = jdbc.queryForObject("select id from produtos where nome = 'Camisa Napoli 26/27'", Long.class);
        jdbc.update("delete from produto_estoques where produto_id = ?", id);
        jdbc.update("update produtos set estoque = 7 where id = ?", id);

        migracoes.run(null);

        List<Map<String, Object>> grade = jdbc.queryForList(
                "select tamanho, quantidade from produto_estoques where produto_id = ?", id);
        assertThat(grade).hasSize(1);
        assertThat(grade.get(0)).containsEntry("TAMANHO", "M").containsEntry("QUANTIDADE", 7);

        // rodar de novo não duplica
        migracoes.run(null);
        assertThat(jdbc.queryForObject("select count(*) from produto_estoques where produto_id = ?", Integer.class, id))
                .isEqualTo(1);
    }

    private List<String> categoriasDe(String produto) {
        return jdbc.queryForList("""
                select c.nome from produto_categorias pc
                join produtos p on p.id = pc.produto_id
                join categorias c on c.id = pc.categoria_id
                where p.nome = ?
                """, String.class, produto);
    }
}
