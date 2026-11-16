package br.edu.atitus.camisa10.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.model.Categoria;
import br.edu.atitus.camisa10.model.ItemPedido;
import br.edu.atitus.camisa10.model.Pedido;
import br.edu.atitus.camisa10.model.Perfil;
import br.edu.atitus.camisa10.model.Personalizacao;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.PedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;
import br.edu.atitus.camisa10.service.ImagemService;

/**
 * Sobe a aplicação com H2 (perfil dev) e confere o seed e o mapeamento JPA.
 * @Transactional desfaz as alterações de cada teste ao final.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DataSeederTest {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImagemService imagemService;

    @Test
    void deveCriarUsuariosDeTesteComSenhaCriptografada() {
        Usuario vendedor = usuarioRepository.findByEmail("vendedor@camisa10.com").orElseThrow();
        Usuario comprador = usuarioRepository.findByEmail("comprador@camisa10.com").orElseThrow();

        assertThat(vendedor.getPerfil()).isEqualTo(Perfil.VENDEDOR);
        assertThat(comprador.getPerfil()).isEqualTo(Perfil.COMPRADOR);
        // a senha não fica em texto puro, mas o BCrypt reconhece
        assertThat(vendedor.getSenhaHash()).isNotEqualTo(DataSeeder.SENHA_PADRAO);
        assertThat(passwordEncoder.matches(DataSeeder.SENHA_PADRAO, vendedor.getSenhaHash())).isTrue();
    }

    @Test
    void deveCriarAsDezCategoriasNaOrdemDoApp() {
        List<String> nomes = categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(Categoria::getNome).toList();

        assertThat(nomes).containsExactly("Brasileirão", "Argentino", "Alemão", "Espanhol", "Francês",
                "Italiano", "Seleções", "Retrô", "Manga Longa", "Manga Longa Retrô");
    }

    @Test
    void deveCriarCamisasDoVendedorComCoresEFotoEmTodasAsCategorias() {
        Usuario vendedor = usuarioRepository.findByEmail("vendedor@camisa10.com").orElseThrow();
        List<Produto> produtos = produtoRepository.findByVendedorIdAndAtivoTrueOrderByNomeAsc(vendedor.getId());

        assertThat(produtos).hasSize(41);
        assertThat(produtos).allSatisfy(p -> {
            assertThat(p.getCorPrincipal()).matches("#[0-9A-F]{6}");
            assertThat(p.getCorTexto()).matches("#[0-9A-F]{6}");
            assertThat(p.getEstoque()).isPositive();
            assertThat(p.getAno()).isBetween(1980, 2026);
            assertThat(p.getMarca()).isNotBlank();
            assertThat(p.getCorFamilia()).isNotBlank();
            // o total é a soma da grade de tamanhos
            assertThat(p.getEstoques().values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(p.getEstoque());
            // a foto foi copiada para a pasta de uploads e existe de verdade
            assertThat(p.getImagemUrl()).startsWith(ImagemService.PREFIXO_URL);
            assertThat(imagemService.getPasta()
                    .resolve(p.getImagemUrl().substring(ImagemService.PREFIXO_URL.length()))).exists();
        });
        // todas as categorias têm pelo menos uma camisa
        assertThat(produtos.stream().flatMap(p -> p.getCategorias().stream()).map(Categoria::getNome).distinct())
                .hasSize(10);
        // camisas com mais de uma categoria
        Produto brasil2002 = produtos.stream().filter(p -> p.getNome().equals("Camisa Retrô Brasil 2002")).findFirst().orElseThrow();
        assertThat(brasil2002.getCategorias()).extracting(Categoria::getNome).containsExactly("Seleções", "Retrô");
        Produto corinthians2000 = produtos.stream().filter(p -> p.getNome().equals("Camisa Retrô Corinthians 2000")).findFirst().orElseThrow();
        assertThat(corinthians2000.getCategorias()).extracting(Categoria::getNome).containsExactly("Brasileirão", "Retrô");
    }

    @Test
    void deveSalvarPedidoComPersonalizacaoECalcularTotal() {
        Usuario comprador = usuarioRepository.findByEmail("comprador@camisa10.com").orElseThrow();
        Produto produto = produtoRepository.findAll().get(0);
        BigDecimal acrescimo = new BigDecimal("25.00");

        Pedido pedido = new Pedido(comprador);
        // 2 camisas personalizadas + 1 sem personalização
        pedido.adicionarItem(new ItemPedido(produto, Tamanho.M, 2, produto.getPreco(), acrescimo,
                new Personalizacao("RONALDINHO", 10)));
        pedido.adicionarItem(new ItemPedido(produto, Tamanho.G, 1, produto.getPreco(), BigDecimal.ZERO, null));
        pedidoRepository.saveAndFlush(pedido);

        Pedido salvo = pedidoRepository.findByCompradorIdOrderByDataCriacaoDescIdDesc(comprador.getId()).get(0);
        BigDecimal esperado = produto.getPreco().add(acrescimo).multiply(BigDecimal.valueOf(2))
                .add(produto.getPreco());

        assertThat(salvo.getValorTotal()).isEqualByComparingTo(esperado);
        assertThat(salvo.getItens()).hasSize(2);
        assertThat(salvo.getItens().get(0).getPersonalizacao().getNome()).isEqualTo("RONALDINHO");
        assertThat(salvo.getItens().get(1).getPersonalizacao()).isNull();
        assertThat(salvo.getItens()).extracting(ItemPedido::getTamanho).containsExactly(Tamanho.M, Tamanho.G);
    }
}
