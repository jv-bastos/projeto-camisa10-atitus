import api from './api';

/** RF06 – números do painel: produtos ativos, número de vendas e faturamento. */
export async function buscarResumo() {
  const resposta = await api.get('/api/vendedor/resumo');
  return resposta.data;
}

/** Produtos ativos do vendedor logado. */
export async function listarMeusProdutos() {
  const resposta = await api.get('/api/vendedor/produtos');
  return resposta.data;
}

/** RF07 – vendas que incluem produtos do vendedor. */
export async function listarVendas() {
  const resposta = await api.get('/api/vendedor/vendas');
  return resposta.data;
}

/**
 * RF04/RF08 – cria (sem id) ou atualiza (com id) um produto.
 * dados: { nome, descricao, preco, estoque, categoriaId, corPrincipal, corTexto }
 */
export async function salvarProduto(id, dados) {
  const resposta = id ? await api.put(`/api/produtos/${id}`, dados) : await api.post('/api/produtos', dados);
  return resposta.data;
}

/** RF08 – exclusão lógica: o produto some do catálogo, os pedidos antigos continuam. */
export async function excluirProduto(id) {
  await api.delete(`/api/produtos/${id}`);
}

/**
 * Catálogo de fotos que acompanha o projeto (backend/catalogo-fotos).
 * Cada foto traz nome e categorias sugeridos para preencher o cadastro.
 */
export async function listarFotosCatalogo({ categoriaId, busca } = {}) {
  const params = {};
  if (categoriaId) params.categoriaId = categoriaId;
  if (busca?.trim()) params.busca = busca.trim();
  const resposta = await api.get('/api/vendedor/fotos-catalogo', { params });
  return resposta.data;
}

/** Usa uma foto do catálogo como imagem do produto (o backend copia o arquivo). */
export async function usarFotoDoCatalogo(produtoId, arquivo) {
  const resposta = await api.post(`/api/produtos/${produtoId}/imagem-catalogo`, { arquivo });
  return resposta.data;
}

/**
 * RF05 – envia a foto escolhida no celular (expo-image-picker).
 * foto: { uri, mimeType, fileName } do resultado do seletor.
 */
export async function enviarFoto(produtoId, foto) {
  const tipo = foto.mimeType || 'image/jpeg';
  const extensao = tipo.split('/')[1] === 'png' ? 'png' : tipo.split('/')[1] === 'webp' ? 'webp' : 'jpg';
  const formulario = new FormData();
  // no React Native, um arquivo no FormData é um objeto { uri, name, type }
  formulario.append('imagem', { uri: foto.uri, name: foto.fileName || `foto.${extensao}`, type: tipo });

  const resposta = await api.post(`/api/produtos/${produtoId}/imagem`, formulario, {
    headers: { 'Content-Type': 'multipart/form-data' },
    transformRequest: (dados) => dados, // não deixa o axios transformar o FormData em JSON
    timeout: 60000, // fotos podem demorar mais em redes lentas
  });
  return resposta.data;
}
