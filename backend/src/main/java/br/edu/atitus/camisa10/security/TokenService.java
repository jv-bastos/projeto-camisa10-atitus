package br.edu.atitus.camisa10.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.edu.atitus.camisa10.model.Usuario;

/**
 * Gera o token JWT entregue no login/cadastro.
 *
 * Conteúdo do token:
 * - sub (subject): id do usuário
 * - perfil: VENDEDOR ou COMPRADOR (vira a role usada nas regras de acesso)
 * - nome e email: apenas informativos
 * - exp: data de expiração
 */
@Service
public class TokenService {

    public static final String CLAIM_PERFIL = "perfil";

    private final JwtEncoder jwtEncoder;
    private final Duration validade;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${camisa10.jwt.expiracao-horas}") long expiracaoHoras) {
        this.jwtEncoder = jwtEncoder;
        this.validade = Duration.ofHours(expiracaoHoras);
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("camisa10")
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .claim("nome", usuario.getNome())
                .claim("email", usuario.getEmail())
                .build();

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }
}
