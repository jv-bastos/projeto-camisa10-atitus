import api from './api';

/** Chamadas de autenticação. Ambas devolvem { token, usuario }. */

export async function login(email, senha) {
  const resposta = await api.post('/api/auth/login', { email, senha });
  return resposta.data;
}

/** dados = { nome, email, senha, perfil: 'VENDEDOR' | 'COMPRADOR' } */
export async function registrar(dados) {
  const resposta = await api.post('/api/auth/registro', dados);
  return resposta.data;
}
