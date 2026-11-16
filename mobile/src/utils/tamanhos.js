/** Grade de tamanhos da loja, na mesma ordem do backend. */
export const TAMANHOS = ['P', 'M', 'G', 'GG', 'XGG'];

/** Unidades de um tamanho (o backend sempre devolve a grade completa em produto.tamanhos). */
export function estoqueDoTamanho(produto, tamanho) {
  return produto.tamanhos?.find((t) => t.tamanho === tamanho)?.quantidade ?? 0;
}

/** "P 2 · M 4 · G 3 · GG 2 · XGG 1" (usado na revisão do cadastro). */
export function descreverGrade(estoques) {
  return TAMANHOS.map((t) => `${t} ${estoques[t] ?? 0}`).join(' · ');
}

/**
 * Guias de tamanhos (medidas em cm), a partir das tabelas do fornecedor.
 * Equivalência: GG = XL e XGG = 2XL. Margem de erro de 1 a 3 cm em cada medida.
 */
export const GUIAS = {
  torcedor: {
    titulo: 'Camisa torcedor',
    colunas: ['Comprimento', 'Largura', 'Sua altura', 'Seu peso (kg)'],
    linhas: {
      P: ['69–71', '53–55', '162–170', '50–62'],
      M: ['71–73', '55–57', '170–176', '62–78'],
      G: ['73–75', '57–58', '176–182', '78–83'],
      GG: ['75–78', '58–60', '182–190', '83–90'],
      XGG: ['78–81', '60–62', '190–195', '90–97'],
    },
  },
  retro: {
    titulo: 'Camisa retrô',
    colunas: ['Comprimento', 'Largura', 'Sua altura'],
    linhas: {
      P: ['67', '48', '160–165'],
      M: ['70', '50', '165–170'],
      G: ['73,5', '52,5', '170–175'],
      GG: ['77', '55', '175–178'],
      XGG: ['80', '57', '179–184'],
    },
  },
};

/** Camisas retrô (inclusive manga longa retrô) usam a tabela retrô; as demais, a de torcedor. */
export function guiaDoProduto(produto) {
  const retro = produto.categorias?.some((c) => c.nome === 'Retrô' || c.nome === 'Manga Longa Retrô');
  return retro ? GUIAS.retro : GUIAS.torcedor;
}
