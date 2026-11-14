package br.edu.atitus.camisa10.service;

import java.util.List;

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

/**
 * Catálogo (RF09, RF10) e CRUD de produtos do vendedor (RF04, RF05, RF08).
 */
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImagemService imagemService;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository,
                          UsuarioRepository usuarioRepository, ImagemService imagemService) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagemService = imagemService;
    }

    // ------------------------------------------------------------------ público

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAllByOrderByIdAsc().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarCatalogo(Long categoriaId, String busca) {
        String termo = busca == null ? "" : busca.trim();
        return produtoRepository.buscarCatalogo(categoriaId, termo).stream().map(ProdutoResponse::de).toList();
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
        Produto produto = new Produto(dados.nome(), dados.descricao(), dados.preco(), dados.estoque(),
                dados.corPrincipal(), dados.corTexto(), buscarCategoria(dados.categoriaId()), vendedor);
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest dados, Long vendedorId) {
        Produto produto = buscarDoVendedor(id, vendedorId);
        produto.setNome(dados.nome());
        produto.setDescricao(dados.descricao());
        produto.setPreco(dados.preco());
        produto.setEstoque(dados.estoque());
        produto.setCategoria(buscarCategoria(dados.categoriaId()));
        produto.setCorPrincipal(dados.corPrincipal());
        produto.setCorTexto(dados.corTexto());
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

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException("Categoria informada não existe."));
    }
}
