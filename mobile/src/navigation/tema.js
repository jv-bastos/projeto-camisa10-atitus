import { DarkTheme } from '@react-navigation/native';

import Icone from '../components/Icone';
import { cores, fontes, tipografia } from '../theme';

/** Tema escuro do React Navigation com as cores e fontes do app. */
export const temaNavegacao = {
  ...DarkTheme,
  colors: {
    ...DarkTheme.colors,
    primary: cores.destaque,
    background: cores.fundo,
    card: cores.barra,
    text: cores.texto,
    border: cores.divisoria,
    notification: cores.destaque,
  },
  fonts: {
    regular: { fontFamily: fontes.media, fontWeight: 'normal' },
    medium: { fontFamily: fontes.negrito, fontWeight: 'normal' },
    bold: { fontFamily: fontes.pesada, fontWeight: 'normal' },
    heavy: { fontFamily: fontes.pesada, fontWeight: 'normal' },
  },
};

/**
 * Opções comuns das barras de abas (vendedor e comprador).
 * icones = { NomeDaAba: 'nome-do-icone' }
 */
export function opcoesDaBarraDeAbas(icones) {
  return ({ route }) => ({
    headerShown: false,
    tabBarActiveTintColor: cores.destaque,
    tabBarInactiveTintColor: cores.textoSecundario,
    tabBarStyle: { backgroundColor: cores.barra, borderTopColor: cores.divisoria, borderTopWidth: 1 },
    tabBarLabelStyle: { ...tipografia.rotulo, fontSize: 10, letterSpacing: 1 },
    tabBarBadgeStyle: { backgroundColor: cores.destaque, color: cores.textoSobreDestaque, fontFamily: fontes.pesada },
    tabBarIcon: ({ color, size }) => <Icone nome={icones[route.name]} cor={color} tamanho={size} espessura={1.8} />,
  });
}
