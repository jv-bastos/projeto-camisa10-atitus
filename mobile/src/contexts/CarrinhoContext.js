import { createContext, useContext, useEffect, useMemo, useState } from 'react';

import { estoqueDoTamanho } from '../utils/tamanhos';
import { useAuth } from './AuthContext';

const CarrinhoContext = createContext(null);

/** Identifica uma linha do carrinho: a mesma camisa em tamanhos diferentes são linhas diferentes. */
export function chaveDoItem(produtoId, tamanho) {
  return `${produtoId}-${tamanho}`;
}

/**
 * RF12 – carrinho do comprador (Context API), guardado em memória.
 * Cada item: { chave, produto, tamanho, quantidade }. A quantidade nunca passa do
 * estoque conhecido daquele tamanho; o backend confere de novo na compra.
 * O carrinho é esvaziado quando o usuário sai da conta.
 */
export function CarrinhoProvider({ children }) {
  const { usuario } = useAuth();
  const [itens, setItens] = useState([]);

  useEffect(() => {
    if (!usuario) setItens([]);
  }, [usuario]);

  const valor = useMemo(() => {
    function adicionar(produto, tamanho, quantidade = 1) {
      const chave = chaveDoItem(produto.id, tamanho);
      const limite = estoqueDoTamanho(produto, tamanho);
      setItens((atuais) => {
        const existente = atuais.find((i) => i.chave === chave);
        if (existente) {
          return atuais.map((i) =>
            i.chave === chave ? { ...i, produto, quantidade: Math.min(i.quantidade + quantidade, limite) } : i,
          );
        }
        return [...atuais, { chave, produto, tamanho, quantidade: Math.min(quantidade, limite) }];
      });
    }

    function alterarQuantidade(chave, quantidade) {
      setItens((atuais) =>
        atuais
          .map((i) =>
            i.chave === chave
              ? { ...i, quantidade: Math.min(quantidade, estoqueDoTamanho(i.produto, i.tamanho)) }
              : i,
          )
          .filter((i) => i.quantidade > 0),
      );
    }

    function remover(chave) {
      setItens((atuais) => atuais.filter((i) => i.chave !== chave));
    }

    function limpar() {
      setItens([]);
    }

    /** Quantas unidades de um produto/tamanho já estão no carrinho. */
    function quantidadeNoCarrinho(produtoId, tamanho) {
      return itens.find((i) => i.chave === chaveDoItem(produtoId, tamanho))?.quantidade ?? 0;
    }

    const totalItens = itens.reduce((soma, i) => soma + i.quantidade, 0);
    const total = itens.reduce((soma, i) => soma + Number(i.produto.preco) * i.quantidade, 0);

    return { itens, totalItens, total, adicionar, alterarQuantidade, remover, limpar, quantidadeNoCarrinho };
  }, [itens]);

  return <CarrinhoContext.Provider value={valor}>{children}</CarrinhoContext.Provider>;
}

export function useCarrinho() {
  const contexto = useContext(CarrinhoContext);
  if (!contexto) {
    throw new Error('useCarrinho precisa estar dentro de <CarrinhoProvider>.');
  }
  return contexto;
}
