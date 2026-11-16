package br.edu.atitus.camisa10.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.dto.produto.FiltroCatalogo;
import br.edu.atitus.camisa10.dto.produto.OpcoesFiltroResponse;
import br.edu.atitus.camisa10.dto.produto.OpcoesFiltroResponse.OpcaoOrdenacao;
import br.edu.atitus.camisa10.dto.produto.ProdutoResponse;
import br.edu.atitus.camisa10.exception.RegraNegocioException;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;
import br.edu.atitus.camisa10.repository.ItemPedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.util.CoresCamisa;
import br.edu.atitus.camisa10.util.Marcas;
import br.edu.atitus.camisa10.util.TextoBusca;

/**
 * Vitrine do comprador (RF09/RF10): busca, filtros e ordenação.
 * O banco já devolve só produtos ativos da categoria/busca; os demais filtros e a
 * ordenação são aplicados aqui (o catálogo de uma loja cabe tranquilamente em memória).
 */
@Service
public class CatalogoService {

    private static final List<OpcaoOrdenacao> ORDENACOES = List.of(
            new OpcaoOrdenacao("nome", "Nome (A–Z)"),
            new OpcaoOrdenacao("nome_desc", "Nome (Z–A)"),
            new OpcaoOrdenacao("preco", "Menor preço"),
            new OpcaoOrdenacao("preco_desc", "Maior preço"),
            new OpcaoOrdenacao("ano_desc", "Mais novas"),
            new OpcaoOrdenacao("ano", "Mais antigas"),
            new OpcaoOrdenacao("vendidas", "Mais vendidas"),
            new OpcaoOrdenacao("lancamentos", "Lançamentos"));

    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public CatalogoService(ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(FiltroCatalogo f) {
        List<Produto> produtos = produtoRepository.buscarCatalogo(f.categoriaId(), TextoBusca.normalizar(f.busca()));
        return produtos.stream()
                .filter(filtro(f))
                .sorted(ordenacao(f.ordenar()))
                .map(ProdutoResponse::de)
                .toList();
    }

    /** Opções do painel de filtros, calculadas a partir das camisas ativas. */
    @Transactional(readOnly = true)
    public OpcoesFiltroResponse opcoes() {
        List<Produto> produtos = produtoRepository.buscarCatalogo(null, "");
        List<String> marcas = produtos.stream().map(Produto::getMarca).filter(Objects::nonNull).distinct()
                .sorted(Comparator.comparingInt(Marcas.TODAS::indexOf)).toList();
        List<String> cores = produtos.stream().map(Produto::getCorFamilia).filter(Objects::nonNull).distinct()
                .sorted(Comparator.comparingInt(CoresCamisa.NOMES::indexOf)).toList();
        return new OpcoesFiltroResponse(
                marcas,
                cores,
                Arrays.stream(Tamanho.values()).map(Enum::name).toList(),
                produtos.stream().map(Produto::getPreco).min(Comparator.naturalOrder()).orElse(null),
                produtos.stream().map(Produto::getPreco).max(Comparator.naturalOrder()).orElse(null),
                produtos.stream().map(Produto::getAno).filter(Objects::nonNull).min(Integer::compare).orElse(null),
                produtos.stream().map(Produto::getAno).filter(Objects::nonNull).max(Integer::compare).orElse(null),
                ORDENACOES);
    }

    private Predicate<Produto> filtro(FiltroCatalogo f) {
        Predicate<Produto> p = produto -> true;
        if (f.precoMin() != null) p = p.and(x -> x.getPreco().compareTo(f.precoMin()) >= 0);
        if (f.precoMax() != null) p = p.and(x -> x.getPreco().compareTo(f.precoMax()) <= 0);
        // com filtro de ano, camisas sem ano cadastrado ficam de fora
        if (f.anoMin() != null) p = p.and(x -> x.getAno() != null && x.getAno() >= f.anoMin());
        if (f.anoMax() != null) p = p.and(x -> x.getAno() != null && x.getAno() <= f.anoMax());
        if (temValores(f.cores())) p = p.and(x -> f.cores().contains(x.getCorFamilia()));
        if (temValores(f.marcas())) p = p.and(x -> f.marcas().contains(x.getMarca()));
        if (f.tamanho() != null) p = p.and(x -> x.getEstoque(f.tamanho()) > 0);
        return p;
    }

    private Comparator<Produto> ordenacao(String ordenar) {
        Comparator<Produto> porNome = Comparator.comparing(Produto::getNomeBusca, Comparator.nullsLast(Comparator.naturalOrder()));
        String criterio = ordenar == null || ordenar.isBlank() ? "nome" : ordenar;
        Comparator<Produto> principal = switch (criterio) {
            case "nome" -> porNome;
            case "nome_desc" -> porNome.reversed();
            case "preco" -> Comparator.comparing(Produto::getPreco);
            case "preco_desc" -> Comparator.comparing(Produto::getPreco, Comparator.reverseOrder());
            // camisas sem ano vão para o fim nas duas direções
            case "ano_desc" -> Comparator.comparing(Produto::getAno, Comparator.nullsLast(Comparator.reverseOrder()));
            case "ano" -> Comparator.comparing(Produto::getAno, Comparator.nullsLast(Comparator.naturalOrder()));
            case "vendidas" -> {
                Map<Long, Long> vendidas = unidadesVendidas();
                yield Comparator.comparing((Produto x) -> vendidas.getOrDefault(x.getId(), 0L), Comparator.reverseOrder());
            }
            case "lancamentos" -> Comparator.comparing(Produto::getId, Comparator.reverseOrder());
            default -> throw new RegraNegocioException("Ordenação inválida: " + criterio + ".");
        };
        return principal.thenComparing(porNome);
    }

    private Map<Long, Long> unidadesVendidas() {
        return itemPedidoRepository.somarUnidadesVendidasPorProduto().stream()
                .collect(Collectors.toMap(linha -> (Long) linha[0], linha -> ((Number) linha[1]).longValue()));
    }

    private static boolean temValores(List<String> lista) {
        return lista != null && lista.stream().anyMatch(v -> v != null && !v.isBlank());
    }
}
