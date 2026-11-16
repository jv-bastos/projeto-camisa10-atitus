package br.edu.atitus.camisa10.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import br.edu.atitus.camisa10.util.CoresCamisa;
import br.edu.atitus.camisa10.util.TextoBusca;

/**
 * Camisa vendida no app.
 *
 * - corPrincipal e corTexto (formato "#RRGGBB") são usadas para desenhar
 *   a prévia da personalização no app.
 * - A exclusão é lógica: "ativo = false" esconde o produto do catálogo
 *   sem quebrar os pedidos antigos que apontam para ele.
 */
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    /** Nome sem acentos e em minúsculas, usado só na busca do catálogo (ver TextoBusca). */
    @Column(name = "nome_busca", length = 120)
    private String nomeBusca;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /** Total de unidades (soma de todos os tamanhos), mantido por setEstoques/baixarEstoque. */
    @Column(nullable = false)
    private Integer estoque;

    /** Unidades por tamanho (P, M, G, GG, XGG). Tamanho ausente = 0 unidades. */
    @ElementCollection
    @CollectionTable(name = "produto_estoques", joinColumns = @JoinColumn(name = "produto_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "tamanho", length = 4)
    @Column(name = "quantidade", nullable = false)
    private Map<Tamanho, Integer> estoques = new EnumMap<>(Tamanho.class);

    @Column(name = "imagem_url")
    private String imagemUrl;

    @Column(name = "cor_principal", nullable = false, length = 7)
    private String corPrincipal;

    /** Cor da paleta mais próxima de corPrincipal (ex.: "Vermelho"); usada no filtro por cor. */
    @Column(name = "cor_familia", length = 20)
    private String corFamilia;

    /** Ano da camisa (temporada 26/27 = 2026; retrô = ano do modelo). Filtro e ordenação. */
    private Integer ano;

    /** Marca do material esportivo (Adidas, Nike...). Opcional. */
    @Column(length = 40)
    private String marca;

    @Column(name = "cor_texto", nullable = false, length = 7)
    private String corTexto;

    @Column(nullable = false)
    private boolean ativo = true;

    /**
     * Uma camisa pode estar em várias categorias (RF09).
     * Ex.: "Retrô Brasil 2002" é Retrô e também Seleções.
     */
    @ManyToMany
    @JoinTable(name = "produto_categorias",
            joinColumns = @JoinColumn(name = "produto_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    @OrderBy("id")
    private Set<Categoria> categorias = new LinkedHashSet<>();

    /** Vendedor dono do produto (RF08: só ele pode editar). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario vendedor;

    /** Construtor exigido pelo JPA. */
    protected Produto() {
    }

    public Produto(String nome, String descricao, BigDecimal preco, Map<Tamanho, Integer> estoques,
                   String corPrincipal, String corTexto, Collection<Categoria> categorias, Usuario vendedor) {
        setNome(nome);
        this.descricao = descricao;
        this.preco = preco;
        setEstoques(estoques);
        setCorPrincipal(corPrincipal);
        this.corTexto = corTexto;
        setCategorias(categorias);
        this.vendedor = vendedor;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        this.nomeBusca = TextoBusca.normalizar(nome);
    }

    public String getNomeBusca() {
        return nomeBusca;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    /** Total de unidades em todos os tamanhos. */
    public Integer getEstoque() {
        return estoque;
    }

    /** Unidades disponíveis em um tamanho (0 se o tamanho não tiver estoque). */
    public int getEstoque(Tamanho tamanho) {
        return estoques.getOrDefault(tamanho, 0);
    }

    public Map<Tamanho, Integer> getEstoques() {
        return estoques;
    }

    /** Troca a grade inteira e recalcula o total. */
    public void setEstoques(Map<Tamanho, Integer> novos) {
        estoques.clear();
        novos.forEach((tamanho, quantidade) -> estoques.put(tamanho, quantidade == null ? 0 : quantidade));
        recalcularTotal();
    }

    /** Baixa unidades vendidas de um tamanho (o PedidoService confere antes se há estoque). */
    public void baixarEstoque(Tamanho tamanho, int quantidade) {
        estoques.put(tamanho, getEstoque(tamanho) - quantidade);
        recalcularTotal();
    }

    private void recalcularTotal() {
        estoque = estoques.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getCorPrincipal() {
        return corPrincipal;
    }

    public void setCorPrincipal(String corPrincipal) {
        this.corPrincipal = corPrincipal;
        this.corFamilia = CoresCamisa.familia(corPrincipal);
    }

    public String getCorFamilia() {
        return corFamilia;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getCorTexto() {
        return corTexto;
    }

    public void setCorTexto(String corTexto) {
        this.corTexto = corTexto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(Collection<Categoria> categorias) {
        this.categorias.clear();
        this.categorias.addAll(categorias);
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }
}
