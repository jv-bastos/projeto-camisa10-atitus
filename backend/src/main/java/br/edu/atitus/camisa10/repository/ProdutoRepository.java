package br.edu.atitus.camisa10.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.atitus.camisa10.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Produtos ativos de um vendedor (RF08). */
    List<Produto> findByVendedorIdAndAtivoTrueOrderByNomeAsc(Long vendedorId);
}
