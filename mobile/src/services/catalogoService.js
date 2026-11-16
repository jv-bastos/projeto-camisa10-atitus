import api from './api';

/** RF09 – categorias para os filtros da vitrine. */
export async function listarCategorias() {
  const resposta = await api.get('/api/categorias');
  return resposta.data;
}

/**
 * RF09/RF10 – produtos ativos do catálogo, com filtros e ordenação (todos opcionais):
 * { categoriaId, busca, precoMin, precoMax, anoMin, anoMax, cores: [], marcas: [], tamanho, ordenar }
 * A busca ignora acentos e maiúsculas (feito no backend).
 */
export async function listarProdutos(filtros = {}) {
  const params = {};
  for (const [chave, valor] of Object.entries(filtros)) {
    if (Array.isArray(valor)) {
      // listas vão como "Branco,Preto", o formato que o Spring entende
      if (valor.length) params[chave] = valor.join(',');
    } else if (valor !== null && valor !== undefined && String(valor).trim() !== '') {
      params[chave] = typeof valor === 'string' ? valor.trim() : valor;
    }
  }
  const resposta = await api.get('/api/produtos', { params });
  return resposta.data;
}

/** Opções do painel "Filtrar e ordenar": marcas e cores existentes, faixas de preço/ano e ordenações. */
export async function buscarOpcoesFiltro() {
  const resposta = await api.get('/api/produtos/filtros');
  return resposta.data;
}

/** RF11 – detalhe de um produto. */
export async function buscarProduto(id) {
  const resposta = await api.get(`/api/produtos/${id}`);
  return resposta.data;
}
