package br.edu.atitus.camisa10.config;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.model.Categoria;
import br.edu.atitus.camisa10.model.Perfil;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;

/**
 * Popula o banco com dados de exemplo na inicialização.
 * Só roda quando o banco está vazio, então não duplica dados no PostgreSQL.
 *
 * Usuários de teste (senha 123456):
 *   vendedor@camisa10.com  (VENDEDOR)
 *   comprador@camisa10.com (COMPRADOR)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    public static final String SENHA_PADRAO = "123456";

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      CategoriaRepository categoriaRepository,
                      ProdutoRepository produtoRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            log.info("Banco já possui dados, seed ignorado.");
            return;
        }

        // --- Usuários ---
        String hash = passwordEncoder.encode(SENHA_PADRAO);
        Usuario vendedor = usuarioRepository.save(
                new Usuario("Loja Camisa 10", "vendedor@camisa10.com", hash, Perfil.VENDEDOR));
        usuarioRepository.save(
                new Usuario("Comprador Teste", "comprador@camisa10.com", hash, Perfil.COMPRADOR));

        // --- Categorias (RF09) ---
        Categoria nacional = categoriaRepository.save(new Categoria("Nacional"));
        Categoria internacional = categoriaRepository.save(new Categoria("Internacional"));
        Categoria selecoes = categoriaRepository.save(new Categoria("Seleções"));
        Categoria retro = categoriaRepository.save(new Categoria("Retrô"));

        // --- Camisas (cor principal / cor do texto usadas na personalização) ---
        criarProduto("Camisa Grêmio I 2026",
                "Camisa tricolor oficial, tecido leve com tecnologia de secagem rápida.",
                "299.90", 20, "#0A6EB4", "#FFFFFF", nacional, vendedor);
        criarProduto("Camisa Internacional I 2026",
                "Camisa vermelha clássica do Colorado, gola careca e escudo bordado.",
                "299.90", 15, "#D00000", "#FFFFFF", nacional, vendedor);
        criarProduto("Camisa Barcelona I 2026",
                "Listras azul-grená tradicionais, modelo torcedor.",
                "349.90", 10, "#A50044", "#EDBB00", internacional, vendedor);
        criarProduto("Camisa Real Madrid I 2026",
                "Camisa branca merengue com detalhes em preto.",
                "349.90", 8, "#F5F5F5", "#1A1A1A", internacional, vendedor);
        criarProduto("Camisa Seleção Brasileira I 2026",
                "A amarelinha da Copa, com o escudo da CBF e as cinco estrelas.",
                "399.90", 25, "#FFDC02", "#009C3B", selecoes, vendedor);
        criarProduto("Camisa Retrô Holanda 1988",
                "Réplica da camisa laranja campeã da Euro 88.",
                "259.90", 5, "#F36C21", "#000000", retro, vendedor);

        log.info("Seed concluído: {} usuários, {} categorias, {} produtos.",
                usuarioRepository.count(), categoriaRepository.count(), produtoRepository.count());
    }

    private void criarProduto(String nome, String descricao, String preco, int estoque,
                              String corPrincipal, String corTexto,
                              Categoria categoria, Usuario vendedor) {
        produtoRepository.save(new Produto(nome, descricao, new BigDecimal(preco), estoque,
                corPrincipal, corTexto, categoria, vendedor));
    }
}
