import * as SecureStore from 'expo-secure-store';
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { Alert } from 'react-native';

import { definirAoExpirarSessao, definirToken } from '../services/api';
import * as authService from '../services/authService';

/**
 * Estado global de autenticação.
 * - O token fica guardado de forma criptografada no celular (expo-secure-store),
 *   então o usuário continua logado ao reabrir o app.
 * - Logout (RF03) = apagar o token do celular; o backend não guarda sessão.
 */
const AuthContext = createContext(null);

const CHAVE_TOKEN = 'camisa10.token';
const CHAVE_USUARIO = 'camisa10.usuario';

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  // true enquanto lemos o token salvo, para não mostrar a tela de login por um instante
  const [carregando, setCarregando] = useState(true);

  const sair = useCallback(async () => {
    definirToken(null);
    setUsuario(null);
    await Promise.all([SecureStore.deleteItemAsync(CHAVE_TOKEN), SecureStore.deleteItemAsync(CHAVE_USUARIO)]);
  }, []);

  // Ao abrir o app: restaura a sessão salva (se o token ainda for válido)
  useEffect(() => {
    definirAoExpirarSessao(() => {
      sair();
      Alert.alert('Sessão expirada', 'Faça login novamente para continuar.');
    });

    async function restaurarSessao() {
      try {
        const [token, usuarioSalvo] = await Promise.all([
          SecureStore.getItemAsync(CHAVE_TOKEN),
          SecureStore.getItemAsync(CHAVE_USUARIO),
        ]);
        if (token && usuarioSalvo && !tokenExpirado(token)) {
          definirToken(token);
          setUsuario(JSON.parse(usuarioSalvo));
        } else if (token) {
          await sair();
        }
      } catch {
        await sair(); // dado corrompido: começa do zero
      } finally {
        setCarregando(false);
      }
    }
    restaurarSessao();
  }, [sair]);

  /** Guarda token e usuário depois de um login ou cadastro bem-sucedido. */
  const iniciarSessao = useCallback(async ({ token, usuario: dadosUsuario }) => {
    definirToken(token);
    await Promise.all([
      SecureStore.setItemAsync(CHAVE_TOKEN, token),
      SecureStore.setItemAsync(CHAVE_USUARIO, JSON.stringify(dadosUsuario)),
    ]);
    setUsuario(dadosUsuario); // a navegação troca de área automaticamente (RF02)
  }, []);

  const entrar = useCallback(
    async (email, senha) => iniciarSessao(await authService.login(email, senha)),
    [iniciarSessao],
  );

  const cadastrar = useCallback(
    async (dados) => iniciarSessao(await authService.registrar(dados)),
    [iniciarSessao],
  );

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, cadastrar, sair }),
    [usuario, carregando, entrar, cadastrar, sair],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

/** Hook para as telas: const { usuario, entrar, sair } = useAuth(); */
export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error('useAuth precisa estar dentro de <AuthProvider>.');
  }
  return contexto;
}

/**
 * Lê a data de expiração ("exp") de dentro do JWT, sem validar a assinatura
 * (quem valida é o backend). Serve só para não restaurar uma sessão já vencida.
 */
function tokenExpirado(token) {
  try {
    const parteDados = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const { exp } = JSON.parse(atob(parteDados.padEnd(Math.ceil(parteDados.length / 4) * 4, '=')));
    return Date.now() >= exp * 1000;
  } catch {
    return false; // se não der para ler, deixa o backend decidir (401 → logout)
  }
}
