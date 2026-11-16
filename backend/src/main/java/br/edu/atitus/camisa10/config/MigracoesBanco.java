package br.edu.atitus.camisa10.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Atualiza bancos criados por versões anteriores do backend (ex.: PostgreSQL já em uso).
 * O "ddl-auto=update" do Hibernate cria tabelas e colunas novas, mas não migra dados
 * nem apaga colunas antigas. Cada etapa abaixo só age se encontrar o formato antigo;
 * em banco novo (ou já migrado) nada acontece. Roda antes do DataSeeder (ordem 1).
 *
 * 1. Categorias: cada camisa tinha UMA categoria (produtos.categoria_id). Agora a relação
 *    é muitos-para-muitos (produto_categorias). Copia o vínculo e apaga a coluna, que era
 *    NOT NULL e impediria cadastrar produtos.
 * 2. Tamanhos: o estoque era um número só. Agora é por tamanho (produto_estoques).
 *    Produtos sem grade recebem o estoque antigo inteiro no tamanho M.
 */
@Component
@Order(1)
public class MigracoesBanco implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracoesBanco.class);

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public MigracoesBanco(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        migrarCategoriaUnica();
        migrarEstoqueSemTamanho();
    }

    private void migrarCategoriaUnica() {
        if (!colunaExiste("produtos", "categoria_id")) {
            return;
        }
        int copiadas = jdbc.update("""
                insert into produto_categorias (produto_id, categoria_id)
                select p.id, p.categoria_id from produtos p
                where p.categoria_id is not null
                  and not exists (select 1 from produto_categorias pc
                                  where pc.produto_id = p.id and pc.categoria_id = p.categoria_id)
                """);
        jdbc.execute("alter table produtos drop column categoria_id");
        log.info("Migração de categorias: {} vínculo(s) copiado(s) e coluna produtos.categoria_id removida.", copiadas);
    }

    private void migrarEstoqueSemTamanho() {
        int migrados = jdbc.update("""
                insert into produto_estoques (produto_id, tamanho, quantidade)
                select p.id, 'M', p.estoque from produtos p
                where not exists (select 1 from produto_estoques e where e.produto_id = p.id)
                """);
        if (migrados > 0) {
            log.info("Migração de tamanhos: {} produto(s) sem grade receberam o estoque antigo no tamanho M.", migrados);
        }
    }

    /** Procura a coluna em minúsculas (PostgreSQL) e em maiúsculas (H2). */
    boolean colunaExiste(String tabela, String coluna) {
        try (Connection conexao = dataSource.getConnection()) {
            DatabaseMetaData meta = conexao.getMetaData();
            for (String[] nomes : new String[][] { { tabela, coluna }, { tabela.toUpperCase(), coluna.toUpperCase() } }) {
                try (ResultSet rs = meta.getColumns(conexao.getCatalog(), null, nomes[0], nomes[1])) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível verificar a estrutura do banco.", e);
        }
    }
}
