/**
 * Testes das regras do app que não dependem de tela (pasta src/utils).
 * Usam o executor de testes que já vem no Node (node:test), sem bibliotecas extras.
 * Rodar: npm test
 */
import assert from 'node:assert/strict';
import { describe, test } from 'node:test';

import { nomeDaCor } from '../src/utils/coresCamisa.js';
import { contarFiltrosAtivos, FILTROS_VAZIOS, filtrosParaApi } from '../src/utils/filtros.js';
import { formatarData, formatarPreco } from '../src/utils/formatar.js';
import { lerPreco } from '../src/utils/preco.js';
import { descreverGrade, estoqueDoTamanho, GUIAS, guiaDoProduto } from '../src/utils/tamanhos.js';

describe('formatar', () => {
  test('preço no formato brasileiro, com milhar', () => {
    assert.equal(formatarPreco(299.9), 'R$ 299,90');
    assert.equal(formatarPreco('1234.5'), 'R$ 1.234,50');
  });

  test('data e hora do servidor', () => {
    assert.equal(formatarData('2026-11-14T10:05:30.123'), '14/11/2026 às 10:05');
  });
});

describe('lerPreco (cadastro de produto)', () => {
  test('aceita vírgula, ponto, milhar e "R$"', () => {
    assert.equal(lerPreco('299,90'), 299.9);
    assert.equal(lerPreco('299.90'), 299.9);
    assert.equal(lerPreco('1.299,90'), 1299.9);
    assert.equal(lerPreco('R$299,90'), 299.9);
  });

  test('recusa texto inválido ou com mais de 2 casas', () => {
    assert.ok(Number.isNaN(lerPreco('abc')));
    assert.ok(Number.isNaN(lerPreco('29,999')));
    assert.ok(Number.isNaN(lerPreco('')));
  });
});

describe('cores', () => {
  test('cor da paleta pelo nome', () => {
    assert.equal(nomeDaCor('#FFFFFF'), 'Branco');
    assert.equal(nomeDaCor('#ffdc02'), 'Amarelo');
  });

  test('cor fora da paleta recebe o nome da mais próxima', () => {
    assert.equal(nomeDaCor('#CB3524'), 'Vermelho');
    assert.equal(nomeDaCor('#9BB7E8'), 'Azul-claro');
  });
});

describe('tamanhos', () => {
  const produto = {
    tamanhos: [
      { tamanho: 'P', quantidade: 0 },
      { tamanho: 'M', quantidade: 2 },
      { tamanho: 'G', quantidade: 2 },
      { tamanho: 'GG', quantidade: 1 },
      { tamanho: 'XGG', quantidade: 0 },
    ],
    categorias: [{ id: 8, nome: 'Retrô' }],
  };

  test('estoque de cada tamanho', () => {
    assert.equal(estoqueDoTamanho(produto, 'M'), 2);
    assert.equal(estoqueDoTamanho(produto, 'P'), 0);
  });

  test('grade descrita na revisão', () => {
    assert.equal(descreverGrade({ P: 1, M: 4, G: 3 }), 'P 1 · M 4 · G 3 · GG 0 · XGG 0');
  });

  test('camisa retrô usa a tabela retrô; as demais, a de torcedor', () => {
    assert.equal(guiaDoProduto(produto), GUIAS.retro);
    assert.equal(guiaDoProduto({ categorias: [{ id: 1, nome: 'Brasileirão' }] }), GUIAS.torcedor);
  });
});

describe('filtros da vitrine', () => {
  test('sem filtros, nada ativo e só a ordenação padrão', () => {
    assert.equal(contarFiltrosAtivos(FILTROS_VAZIOS), 0);
    assert.equal(filtrosParaApi(FILTROS_VAZIOS).ordenar, 'nome');
  });

  test('conta grupos de filtros e converte para a API', () => {
    const filtros = { ...FILTROS_VAZIOS, precoMax: 'R$400,00', anoMin: '2000', cores: ['Amarelo'], tamanho: 'G' };
    assert.equal(contarFiltrosAtivos(filtros), 4);
    const api = filtrosParaApi(filtros);
    assert.equal(api.precoMax, 400);
    assert.equal(api.precoMin, '');
    assert.deepEqual(api.cores, ['Amarelo']);
    assert.equal(api.tamanho, 'G');
  });
});
