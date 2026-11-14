package br.edu.atitus.camisa10.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.atitus.camisa10.model.Produto;
import jakarta.persistence.LockModeType;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Produtos ativos de um vendedor (RF08). */
    List<Produto> findByVendedorIdAndAtivoTrueOrderByNomeAsc(Long vendedorId);

    /** Total de produtos ativos do vendedor (dashboard – RF06). */
    long countByVendedorIdAndAtivoTrue(Long vendedorId);

    /**
     * Catálogo (RF09): apenas produtos ativos, com filtro opcional por categoria
     * e busca por parte do nome, sem diferenciar maiúsculas nem acentos
     * (compara com nomeBusca; o termo deve vir normalizado por TextoBusca).
     * Quando busca é "", o LIKE '%%' aceita todos os nomes.
     */
    @Query("""
            select p from Produto p join fetch p.categoria c
            where p.ativo = true
              and (:categoriaId is null or c.id = :categoriaId)
              and p.nomeBusca like concat('%', :busca, '%')
            order by p.nome
            """)
    List<Produto> buscarCatalogo(@Param("categoriaId") Long categoriaId, @Param("busca") String busca);

    /**
     * Busca o produto TRAVANDO a linha no banco até o fim da transação da compra.
     * Assim, dois pedidos simultâneos não conseguem vender a mesma última unidade.
     */
    /** Produtos gravados antes de existir a coluna nome_busca (bancos PostgreSQL antigos). */
    List<Produto> findByNomeBuscaIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarParaCompra(@Param("id") Long id);
}
