import axios from 'axios';

import { API_URL } from '../config';

/**
 * Cliente HTTP único do app. Todas as chamadas à API passam por aqui.
 * - adiciona o token JWT em cada requisição (quando o usuário está logado);
 * - avisa o AuthContext quando o servidor responde 401 (sessão expirada).
 */
const api = axios.create({
  baseURL: API_URL,
  timeout: 15000,
});

let tokenAtual = null;
let aoExpirarSessao = null;

/** Chamado pelo AuthContext no login/logout. */
export function definirToken(token) {
  tokenAtual = token;
}

/** O AuthContext registra aqui o que fazer quando a sessão expira. */
export function definirAoExpirarSessao(funcao) {
  aoExpirarSessao = funcao;
}

api.interceptors.request.use((config) => {
  if (tokenAtual) {
    config.headers.Authorization = `Bearer ${tokenAtual}`;
  }
  return config;
});

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    // 401 com token = token vencido ou inválido. (No login errado não há token, então não entra aqui.)
    if (erro.response?.status === 401 && tokenAtual && aoExpirarSessao) {
      aoExpirarSessao();
    }
    return Promise.reject(erro);
  },
);

/** Mensagem amigável para mostrar ao usuário, vinda do backend (campo "mensagem") ou da conexão. */
export function mensagemDeErro(erro) {
  if (!erro.response) {
    return 'Não foi possível conectar ao servidor. Verifique se a API está rodando e o endereço dela (mobile/.env.local ou src/config.js).';
  }
  return erro.response.data?.mensagem || 'Ocorreu um erro inesperado. Tente novamente.';
}

/** Erros de validação por campo devolvidos pelo backend: { email: '...', senha: '...' }. */
export function errosDosCampos(erro) {
  return erro.response?.data?.campos || {};
}

/** O backend devolve imagens como caminho relativo ("/uploads/x.jpg"); aqui viram URL completa. */
export function urlDaImagem(caminho) {
  return caminho ? `${API_URL}${caminho}` : null;
}

export default api;
