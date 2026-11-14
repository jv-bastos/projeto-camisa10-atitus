package br.edu.atitus.camisa10.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.dto.auth.LoginRequest;
import br.edu.atitus.camisa10.dto.auth.LoginResponse;
import br.edu.atitus.camisa10.dto.auth.RegistroRequest;
import br.edu.atitus.camisa10.dto.auth.UsuarioResponse;
import br.edu.atitus.camisa10.exception.ConflitoException;
import br.edu.atitus.camisa10.exception.CredenciaisInvalidasException;
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.UsuarioRepository;
import br.edu.atitus.camisa10.security.TokenService;

/**
 * Cadastro (RF01) e login (RF02).
 * O logout (RF03) acontece no app, apagando o token salvo no celular.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /** Cria o usuário e já devolve um token, para o app entrar direto após o cadastro. */
    @Transactional
    public LoginResponse registrar(RegistroRequest dados) {
        // o e-mail já chega sem espaços e em minúsculas (ver RegistroRequest)
        if (usuarioRepository.existsByEmail(dados.email())) {
            throw new ConflitoException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario(dados.nome(), dados.email(),
                passwordEncoder.encode(dados.senha()), dados.perfil());
        usuarioRepository.save(usuario);

        return gerarResposta(usuario);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest dados) {
        Usuario usuario = usuarioRepository.findByEmail(dados.email())
                .orElseThrow(CredenciaisInvalidasException::new);

        // mesma mensagem para e-mail inexistente e senha errada (não revela quais e-mails existem)
        if (!passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return gerarResposta(usuario);
    }

    private LoginResponse gerarResposta(Usuario usuario) {
        return new LoginResponse(tokenService.gerarToken(usuario), UsuarioResponse.de(usuario));
    }
}
