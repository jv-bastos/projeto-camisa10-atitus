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
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.PedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;

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
    void deveCriarAsQuatroCategorias() {
        List<String> nomes = categoriaRepository.findAllByOrderByIdAsc().stream()
                .map(Categoria::getNome).toList();

        assertThat(nomes).containsExactly("Nacional", "Internacional", "Seleções", "Retrô");
    }

    @Test
    void deveCriarSeisCamisasDoVendedorComCoresDefinidas() {
        Usuario vendedor = usuarioRepository.findByEmail("vendedor@camisa10.com").orElseThrow();
        List<Produto> produtos = produtoRepository.findByVendedorIdAndAtivoTrueOrderByNomeAsc(vendedor.getId());

        assertThat(produtos).hasSize(6);
        assertThat(produtos).allSatisfy(p -> {
            assertThat(p.getCorPrincipal()).matches("#[0-9A-F]{6}");
            assertThat(p.getCorTexto()).matches("#[0-9A-F]{6}");
            assertThat(p.getEstoque()).isPositive();
        });
        // todas as categorias têm pelo menos uma camisa
        assertThat(produtos.stream().map(p -> p.getCategoria().getNome()).distinct()).hasSize(4);
    }

    @Test
    void deveSalvarPedidoComPersonalizacaoECalcularTotal() {
        Usuario comprador = usuarioRepository.findByEmail("comprador@camisa10.com").orElseThrow();
        Produto produto = produtoRepository.findAll().get(0);
        BigDecimal acrescimo = new BigDecimal("25.00");

        Pedido pedido = new Pedido(comprador);
        // 2 camisas personalizadas + 1 sem personalização
        pedido.adicionarItem(new ItemPedido(produto, 2, produto.getPreco(), acrescimo,
                new Personalizacao("RONALDINHO", 10)));
        pedido.adicionarItem(new ItemPedido(produto, 1, produto.getPreco(), BigDecimal.ZERO, null));
        pedidoRepository.saveAndFlush(pedido);

        Pedido salvo = pedidoRepository.findByCompradorIdOrderByDataCriacaoDesc(comprador.getId()).get(0);
        BigDecimal esperado = produto.getPreco().add(acrescimo).multiply(BigDecimal.valueOf(2))
                .add(produto.getPreco());

        assertThat(salvo.getValorTotal()).isEqualByComparingTo(esperado);
        assertThat(salvo.getItens()).hasSize(2);
        assertThat(salvo.getItens().get(0).getPersonalizacao().getNome()).isEqualTo("RONALDINHO");
        assertThat(salvo.getItens().get(1).getPersonalizacao()).isNull();
    }
}
