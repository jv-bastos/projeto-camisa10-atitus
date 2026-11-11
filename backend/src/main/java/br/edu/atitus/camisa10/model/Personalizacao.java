package br.edu.atitus.camisa10.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Nome e número estampados nas costas da camisa (RF11).
 * Relação 1:1 opcional com ItemPedido.
 *
 * Regras (revalidadas no backend):
 * - nome: até 12 caracteres, apenas letras e espaço, salvo em maiúsculas;
 * - numero: inteiro de 0 a 99.
 */
@Entity
@Table(name = "personalizacoes")
public class Personalizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String nome;

    @Column(nullable = false)
    private Integer numero;

    /** Construtor exigido pelo JPA. */
    protected Personalizacao() {
    }

    public Personalizacao(String nome, Integer numero) {
        this.nome = nome;
        this.numero = numero;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Integer getNumero() {
        return numero;
    }
}
