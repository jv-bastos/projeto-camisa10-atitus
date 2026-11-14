package br.edu.atitus.camisa10.service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.edu.atitus.camisa10.dto.pedido.PersonalizacaoRequest;
import br.edu.atitus.camisa10.exception.RegraNegocioException;
import br.edu.atitus.camisa10.model.Personalizacao;

/**
 * Regras da personalização de nome e número (RF11).
 *
 * O app já valida enquanto o usuário digita, mas o backend NÃO confia nisso:
 * toda personalização recebida em um pedido passa por aqui de novo.
 * - nome: obrigatório, até N caracteres (config), só letras e espaço, salvo em MAIÚSCULAS;
 * - número: inteiro de 0 a 99.
 */
@Service
public class PersonalizacaoService {

    public static final int NUMERO_MINIMO = 0;
    public static final int NUMERO_MAXIMO = 99;

    /** \p{L} = qualquer letra, inclusive acentuadas (ex.: JOÃO). */
    private static final Pattern SO_LETRAS_E_ESPACO = Pattern.compile("^[\\p{L} ]+$");
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final BigDecimal valorAcrescimo;
    private final int maxCaracteresNome;

    public PersonalizacaoService(
            @Value("${camisa10.personalizacao.valor-acrescimo}") BigDecimal valorAcrescimo,
            @Value("${camisa10.personalizacao.max-caracteres-nome}") int maxCaracteresNome) {
        this.valorAcrescimo = valorAcrescimo;
        this.maxCaracteresNome = maxCaracteresNome;
    }

    /**
     * Valida e normaliza a personalização.
     * @return a entidade pronta para salvar, ou null se o item não for personalizado.
     * @throws RegraNegocioException se nome ou número forem inválidos.
     */
    public Personalizacao validar(PersonalizacaoRequest dados) {
        if (dados == null) {
            return null; // personalização é opcional
        }

        String nome = dados.nome() == null ? "" : dados.nome().trim().replaceAll(" {2,}", " ");
        if (nome.isEmpty()) {
            throw new RegraNegocioException("Informe o nome da personalização.");
        }
        if (!SO_LETRAS_E_ESPACO.matcher(nome).matches()) {
            throw new RegraNegocioException("O nome da personalização deve conter apenas letras e espaços.");
        }
        nome = nome.toUpperCase(PT_BR);
        if (nome.length() > maxCaracteresNome) {
            throw new RegraNegocioException(
                    "O nome da personalização deve ter no máximo " + maxCaracteresNome + " caracteres.");
        }

        Integer numero = dados.numero();
        if (numero == null) {
            throw new RegraNegocioException("Informe o número da personalização.");
        }
        if (numero < NUMERO_MINIMO || numero > NUMERO_MAXIMO) {
            throw new RegraNegocioException(
                    "O número da personalização deve estar entre " + NUMERO_MINIMO + " e " + NUMERO_MAXIMO + ".");
        }

        return new Personalizacao(nome, numero);
    }

    /** Acréscimo cobrado por camisa personalizada (configurável no application.properties). */
    public BigDecimal getValorAcrescimo() {
        return valorAcrescimo;
    }

    public int getMaxCaracteresNome() {
        return maxCaracteresNome;
    }
}
