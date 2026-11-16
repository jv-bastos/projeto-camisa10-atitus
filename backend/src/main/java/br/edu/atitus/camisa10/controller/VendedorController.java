package br.edu.atitus.camisa10.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.camisa10.dto.produto.FotoCatalogoResponse;
import br.edu.atitus.camisa10.dto.produto.ProdutoResponse;
import br.edu.atitus.camisa10.dto.vendedor.ResumoVendedorResponse;
import br.edu.atitus.camisa10.dto.vendedor.VendaResponse;
import br.edu.atitus.camisa10.security.UsuarioLogado;
import br.edu.atitus.camisa10.service.CatalogoFotosService;
import br.edu.atitus.camisa10.service.ProdutoService;
import br.edu.atitus.camisa10.service.VendaService;

/** Área do VENDEDOR (acesso restrito no SecurityConfig). Sempre filtra pelo vendedor do token. */
@RestController
@RequestMapping("/api/vendedor")
public class VendedorController {

    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final CatalogoFotosService catalogoFotosService;

    public VendedorController(ProdutoService produtoService, VendaService vendaService,
                              CatalogoFotosService catalogoFotosService) {
        this.produtoService = produtoService;
        this.vendaService = vendaService;
        this.catalogoFotosService = catalogoFotosService;
    }

    /** GET /api/vendedor/produtos – apenas os produtos deste vendedor (RF08) */
    @GetMapping("/produtos")
    public List<ProdutoResponse> meusProdutos(@AuthenticationPrincipal Jwt token) {
        return produtoService.listarDoVendedor(UsuarioLogado.id(token));
    }

    /** GET /api/vendedor/vendas (RF07) */
    @GetMapping("/vendas")
    public List<VendaResponse> vendas(@AuthenticationPrincipal Jwt token) {
        return vendaService.listarVendas(UsuarioLogado.id(token));
    }

    /** GET /api/vendedor/resumo (RF06) */
    /** GET /api/vendedor/fotos-catalogo?categoriaId=&busca= – fotos prontas para usar no cadastro */
    @GetMapping("/fotos-catalogo")
    public List<FotoCatalogoResponse> fotosCatalogo(@RequestParam(required = false) Long categoriaId,
                                                    @RequestParam(required = false) String busca) {
        return catalogoFotosService.listar(categoriaId, busca);
    }

    @GetMapping("/resumo")
    public ResumoVendedorResponse resumo(@AuthenticationPrincipal Jwt token) {
        return vendaService.resumo(UsuarioLogado.id(token));
    }
}
