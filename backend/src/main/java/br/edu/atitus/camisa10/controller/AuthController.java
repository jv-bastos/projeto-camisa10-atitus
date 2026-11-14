package br.edu.atitus.camisa10.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.camisa10.dto.auth.LoginRequest;
import br.edu.atitus.camisa10.dto.auth.LoginResponse;
import br.edu.atitus.camisa10.dto.auth.RegistroRequest;
import br.edu.atitus.camisa10.service.AuthService;
import jakarta.validation.Valid;

/** Endpoints públicos de autenticação. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/registro → 201 { token, usuario } */
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse registrar(@Valid @RequestBody RegistroRequest dados) {
        return authService.registrar(dados);
    }

    /** POST /api/auth/login → 200 { token, usuario } */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest dados) {
        return authService.login(dados);
    }
}
