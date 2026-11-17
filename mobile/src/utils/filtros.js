import { lerPreco } from './preco.js';

/** Filtros da vitrine sem nada escolhido (ordenação padrão: nome A–Z). */
export const FILTROS_VAZIOS = {
  ordenar: 'nome',
  precoMin: '',
  precoMax: '',
  anoMin: '',
  anoMax: '',
  cores: [],
  marcas: [],
  tamanho: null,
};

/** Quantos filtros estão ativos (número no botão "Filtrar"). A ordenação não conta. */
export function contarFiltrosAtivos(f) {
  return (
    (f.precoMin || f.precoMax ? 1 : 0) +
    (f.anoMin || f.anoMax ? 1 : 0) +
    (f.cores.length ? 1 : 0) +
    (f.marcas.length ? 1 : 0) +
    (f.tamanho ? 1 : 0)
  );
}

/** Preço digitado no painel → número para a API ('' quando vazio ou inválido). */
function precoParaApi(texto) {
  const valor = lerPreco(texto);
  return Number.isNaN(valor) ? '' : valor;
}

/** Converte os valores do painel para os parâmetros de GET /api/produtos. */
export function filtrosParaApi(f) {
  return {
    ordenar: f.ordenar,
    precoMin: precoParaApi(f.precoMin),
    precoMax: precoParaApi(f.precoMax),
    anoMin: f.anoMin,
    anoMax: f.anoMax,
    cores: f.cores,
    marcas: f.marcas,
    tamanho: f.tamanho,
  };
}
