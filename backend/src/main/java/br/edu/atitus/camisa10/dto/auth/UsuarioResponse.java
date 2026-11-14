package br.edu.atitus.camisa10.dto.auth;

import br.edu.atitus.camisa10.model.Perfil;
import br.edu.atitus.camisa10.model.Usuario;

/** Dados públicos do usuário. Nunca inclui o hash da senha. */
public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
