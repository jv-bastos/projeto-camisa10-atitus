package br.edu.atitus.camisa10.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.edu.atitus.camisa10.dto.produto.FiltroCatalogo;
import br.edu.atitus.camisa10.dto.produto.FotoCatalogoRequest;
import br.edu.atitus.camisa10.dto.produto.OpcoesFiltroResponse;
import br.edu.atitus.camisa10.dto.produto.ProdutoRequest;
import br.edu.atitus.camisa10.dto.produto.ProdutoResponse;
import br.edu.atitus.camisa10.security.UsuarioLogado;
import br.edu.atitus.camisa10.model.Tamanho;
import br.edu.atitus.camisa10.service.CatalogoService;
import br.edu.atitus.camisa10.service.ProdutoService;
import jakarta.validation.Valid;

/**
 * GET são públicos (catálogo). POST/PUT/DELETE exigem perfil VENDEDOR,
 * regra garantida no SecurityConfig (RF15). A posse do produto (RF08)
 * é verificada no ProdutoService.
 */
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CatalogoService catalogoService;

    public ProdutoController(ProdutoService produtoService, CatalogoService catalogoService) {
        this.produtoService = produtoService;
        this.catalogoService = catalogoService;
    }

    /**
     * GET /api/produtos – vitrine com filtros e ordenação, todos opcionais:
     * ?categoriaId=&busca=&precoMin=&precoMax=&anoMin=&anoMax=&cores=Branco,Preto&marcas=Nike&tamanho=GG&ordenar=preco
     */
    @GetMapping
    public List<ProdutoResponse> listar(@RequestParam(required = false) Long categoriaId,
                                        @RequestParam(required = false) String busca,
                                        @RequestParam(required = false) BigDecimal precoMin,
                                        @RequestParam(required = false) BigDecimal precoMax,
                                        @RequestParam(required = false) Integer anoMin,
                                        @RequestParam(required = false) Integer anoMax,
                                        @RequestParam(required = false) List<String> cores,
                                        @RequestParam(required = false) List<String> marcas,
                                        @RequestParam(required = false) Tamanho tamanho,
                                        @RequestParam(required = false) String ordenar) {
        return catalogoService.listar(new FiltroCatalogo(categoriaId, busca, precoMin, precoMax, anoMin, anoMax,
                cores, marcas, tamanho, ordenar));
    }

    /** GET /api/produtos/filtros – opções do painel "Filtrar e ordenar" (marcas e cores existentes, faixas). */
    @GetMapping("/filtros")
    public OpcoesFiltroResponse opcoesDeFiltro() {
        return catalogoService.opcoes();
    }

    /** GET /api/produtos/{id} */
    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return produtoService.buscarPorId(id);
    }

    /** POST /api/produtos → 201 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse criar(@Valid @RequestBody ProdutoRequest dados, @AuthenticationPrincipal Jwt token) {
        return produtoService.criar(dados, UsuarioLogado.id(token));
    }

    /** PUT /api/produtos/{id} */
    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest dados,
                                     @AuthenticationPrincipal Jwt token) {
        return produtoService.atualizar(id, dados, UsuarioLogado.id(token));
    }

    /** DELETE /api/produtos/{id} → 204 (exclusão lógica) */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt token) {
        produtoService.excluir(id, UsuarioLogado.id(token));
    }

    /** POST /api/produtos/{id}/imagem (multipart/form-data, campo "imagem") */
    @PostMapping(value = "/{id}/imagem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProdutoResponse enviarImagem(@PathVariable Long id, @RequestPart("imagem") MultipartFile imagem,
                                        @AuthenticationPrincipal Jwt token) {
        return produtoService.atualizarImagem(id, imagem, UsuarioLogado.id(token));
    }

    /** POST /api/produtos/{id}/imagem-catalogo  { "arquivo": "..." } – usa uma foto do catálogo do projeto */
    @PostMapping("/{id}/imagem-catalogo")
    public ProdutoResponse usarFotoDoCatalogo(@PathVariable Long id, @Valid @RequestBody FotoCatalogoRequest dados,
                                              @AuthenticationPrincipal Jwt token) {
        return produtoService.usarFotoDoCatalogo(id, dados.arquivo(), UsuarioLogado.id(token));
    }
}
