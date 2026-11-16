import api from './api';

/**
 * RF13 – finaliza a compra. O preço e o total são calculados no backend;
 * o app envia só produto e quantidade de cada item.
 * itens: [{ produtoId, quantidade }]
 */
export async function criarPedido(itens) {
  const resposta = await api.post('/api/pedidos', { itens });
  return resposta.data;
}

/** RF14 – histórico de pedidos do comprador logado (mais recentes primeiro). */
export async function listarMeusPedidos() {
  const resposta = await api.get('/api/pedidos/meus');
  return resposta.data;
}
