package br.edu.atitus.camisa10.service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.edu.atitus.camisa10.dto.produto.CategoriaResponse;
import br.edu.atitus.camisa10.dto.produto.ProdutoRequest;
import br.edu.atitus.camisa10.dto.produto.ProdutoResponse;
import br.edu.atitus.camisa10.exception.AcessoNegadoException;
import br.edu.atitus.camisa10.exception.RecursoNaoEncontradoException;
import br.edu.atitus.camisa10.exception.RegraNegocioException;
import br.edu.atitus.camisa10.model.Categoria;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;
import br.edu.atitus.camisa10.util.Marcas;

/**
 * Catálogo (RF09, RF10) e CRUD de produtos do vendedor (RF04, RF05, RF08).
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImagemService imagemService;
    private final CatalogoFotosService catalogoFotosService;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository,
                          UsuarioRepository usuarioRepository, ImagemService imagemService,
                          CatalogoFotosService catalogoFotosService) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagemService = imagemService;
        this.catalogoFotosService = catalogoFotosService;
    }

    // ------------------------------------------------------------------ público

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByIdAsc().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.de(buscarAtivo(id));
    }

    // ---------------------------------------------------------------- vendedor

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarDoVendedor(Long vendedorId) {
        return produtoRepository.findByVendedorIdAndAtivoTrueOrderByNomeAsc(vendedorId).stream()
                .map(ProdutoResponse::de).toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest dados, Long vendedorId) {
        Usuario vendedor = usuarioRepository.getReferenceById(vendedorId);
        Produto produto = new Produto(dados.nome(), dados.descricao(), dados.preco(), dados.estoques(),
                dados.corPrincipal(), dados.corTexto(), buscarCategorias(dados.categoriaIds()), vendedor);
        produto.setAno(dados.ano());
        produto.setMarca(validarMarca(dados.marca()));
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest dados, Long vendedorId) {
        Produto produto = buscarDoVendedor(id, vendedorId);
        produto.setNome(dados.nome());
        produto.setDescricao(dados.descricao());
        produto.setPreco(dados.preco());
        produto.setEstoques(dados.estoques());
        produto.setCategorias(buscarCategorias(dados.categoriaIds()));
        produto.setCorPrincipal(dados.corPrincipal());
        produto.setCorTexto(dados.corTexto());
        produto.setAno(dados.ano());
        produto.setMarca(validarMarca(dados.marca()));
        return ProdutoResponse.de(produto); // alterações salvas automaticamente no fim da transação
    }

    /** Exclusão LÓGICA: o produto some do catálogo, mas os pedidos antigos continuam íntegros. */
    @Transactional
    public void excluir(Long id, Long vendedorId) {
        buscarDoVendedor(id, vendedorId).setAtivo(false);
    }

    @Transactional
    public ProdutoResponse atualizarImagem(Long id, MultipartFile arquivo, Long vendedorId) {
        Produto produto = buscarDoVendedor(id, vendedorId);
        String imagemAntiga = produto.getImagemUrl();
        produto.setImagemUrl(imagemService.salvar(arquivo));
        imagemService.excluir(imagemAntiga);
        return ProdutoResponse.de(produto);
    }

    /** Usa uma foto do catálogo do projeto como imagem do produto (alternativa ao upload). */
    @Transactional
    public ProdutoResponse usarFotoDoCatalogo(Long id, String arquivo, Long vendedorId) {
        Produto produto = buscarDoVendedor(id, vendedorId);
        String imagemAntiga = produto.getImagemUrl();
        produto.setImagemUrl(catalogoFotosService.copiarParaUploads(arquivo));
        imagemService.excluir(imagemAntiga);
        return ProdutoResponse.de(produto);
    }

    // ---------------------------------------------------------------- auxiliares

    private Produto buscarAtivo(Long id) {
        return produtoRepository.findById(id)
                .filter(Produto::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }

    /** RF08: o vendedor só pode mexer nos produtos que ele mesmo cadastrou. */
    private Produto buscarDoVendedor(Long id, Long vendedorId) {
        Produto produto = buscarAtivo(id);
        if (!produto.getVendedor().getId().equals(vendedorId)) {
            throw new AcessoNegadoException("Você só pode alterar os produtos que você cadastrou.");
        }
        return produto;
    }

    /** A marca é opcional, mas, se vier, precisa ser uma das conhecidas (mantém o filtro organizado). */
    private String validarMarca(String marca) {
        if (marca != null && !Marcas.TODAS.contains(marca)) {
            throw new RegraNegocioException("Marca inválida. Escolha uma das opções da lista.");
        }
        return marca;
    }

    /** Busca as categorias escolhidas, na ordem de cadastro; recusa ids que não existem. */
    private List<Categoria> buscarCategorias(List<Long> ids) {
        Set<Long> unicos = new LinkedHashSet<>(ids);
        List<Categoria> encontradas = categoriaRepository.findAllById(unicos).stream()
                .sorted(Comparator.comparing(Categoria::getId)).toList();
        if (encontradas.size() != unicos.size()) {
            throw new RegraNegocioException("Categoria informada não existe.");
        }
        return encontradas;
    }
}
