package br.edu.atitus.camisa10.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.camisa10.dto.produto.CategoriaResponse;
import br.edu.atitus.camisa10.service.ProdutoService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ProdutoService produtoService;

    public CategoriaController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /** GET /api/categorias (público) */
    @GetMapping
    public List<CategoriaResponse> listar() {
        return produtoService.listarCategorias();
    }
}
