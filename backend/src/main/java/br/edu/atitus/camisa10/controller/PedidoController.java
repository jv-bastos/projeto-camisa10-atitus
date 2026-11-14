package br.edu.atitus.camisa10.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.camisa10.dto.pedido.PedidoRequest;
import br.edu.atitus.camisa10.dto.pedido.PedidoResponse;
import br.edu.atitus.camisa10.security.UsuarioLogado;
import br.edu.atitus.camisa10.service.PedidoService;
import jakarta.validation.Valid;

/** Endpoints do COMPRADOR (acesso restrito no SecurityConfig). */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** POST /api/pedidos → 201 com o pedido criado */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody PedidoRequest dados, @AuthenticationPrincipal Jwt token) {
        return pedidoService.criar(dados, UsuarioLogado.id(token));
    }

    /** GET /api/pedidos/meus */
    @GetMapping("/meus")
    public List<PedidoResponse> meusPedidos(@AuthenticationPrincipal Jwt token) {
        return pedidoService.listarDoComprador(UsuarioLogado.id(token));
    }
}
