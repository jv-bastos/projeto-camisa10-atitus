package br.edu.atitus.camisa10.model;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Uma linha do pedido.
 *
 * precoUnitario e valorPersonalizacao são copiados no momento da compra:
 * se o vendedor mudar o preço depois, o histórico continua correto.
 * valorPersonalizacao é cobrado POR UNIDADE (zero quando não há personalização).
 */
@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "valor_personalizacao", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPersonalizacao = BigDecimal.ZERO;

    /** Opcional: null quando a camisa não foi personalizada. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    @JoinColumn(name = "personalizacao_id", unique = true)
    private Personalizacao personalizacao;

    /** Construtor exigido pelo JPA. */
    protected ItemPedido() {
    }

    public ItemPedido(Produto produto, Integer quantidade, BigDecimal precoUnitario,
                      BigDecimal valorPersonalizacao, Personalizacao personalizacao) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.valorPersonalizacao = valorPersonalizacao;
        this.personalizacao = personalizacao;
    }

    /** Subtotal da linha: (preço + acréscimo da personalização) x quantidade. */
    public BigDecimal getSubtotal() {
        return precoUnitario.add(valorPersonalizacao).multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    /** Usado por Pedido.adicionarItem para manter os dois lados da relação. */
    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getValorPersonalizacao() {
        return valorPersonalizacao;
    }

    public Personalizacao getPersonalizacao() {
        return personalizacao;
    }
}
