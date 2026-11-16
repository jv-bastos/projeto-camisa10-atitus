/**
 * Identidade visual do Camisa 10 – "Arquibancada × Vestiário".
 * Noite de jogo: gramado escuro, linhas de cal e o amarelo do refletor.
 * Tipografia: Manrope (fina contra pesada) e DM Mono para preços e rótulos.
 * Todas as cores, fontes, espaçamentos e estilos de texto do app saem daqui.
 */

export const cores = {
  fundo: '#0B1F14', // gramado à noite
  superficie: '#12301F', // cartões e campos
  superficieAlta: '#173B27', // item selecionado / pressionado
  barra: '#081A10', // barra de abas e rodapés fixos
  borda: '#234A33',
  bordaForte: '#3E5A49',
  divisoria: '#1C3A28',

  texto: '#EEF3EC',
  textoSecundario: '#9DB3A4',
  cal: '#E9F0E6', // linhas do campo (usar com opacidade baixa)

  destaque: '#F5C518', // amarelo-refletor: ação principal e preços
  textoSobreDestaque: '#0B1F14',
  sucesso: '#5BD68A',
  erro: '#F26B6B',
};

/**
 * Nomes das fontes carregadas no App.js (useFonts).
 * No React Native, cada peso é uma família própria: use fontFamily, não fontWeight.
 */
export const fontes = {
  fina: 'Manrope_300Light',
  media: 'Manrope_500Medium',
  negrito: 'Manrope_700Bold',
  pesada: 'Manrope_800ExtraBold',
  mono: 'DMMono_400Regular',
  monoMedia: 'DMMono_500Medium',
};

/** Espaçamentos em múltiplos de 4. */
export const espacos = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
};

export const raios = {
  sm: 4,
  md: 6,
  lg: 12,
  redondo: 999,
};

/**
 * Estilos de texto. O contraste fino × pesado é a assinatura do app:
 * ex.: "A" (tituloFino) + "vitrine" (tituloForte) na mesma linha.
 */
export const tipografia = {
  tituloFino: { fontFamily: fontes.fina, fontSize: 34, letterSpacing: -1.5, lineHeight: 38 },
  tituloForte: { fontFamily: fontes.pesada, fontSize: 34, letterSpacing: -1.5, lineHeight: 38 },
  titulo: { fontFamily: fontes.fina, fontSize: 26, letterSpacing: -1, lineHeight: 30 },
  subtitulo: { fontFamily: fontes.negrito, fontSize: 17 },
  corpo: { fontFamily: fontes.media, fontSize: 16 },
  pequeno: { fontFamily: fontes.media, fontSize: 13 },
  rotulo: { fontFamily: fontes.mono, fontSize: 11, letterSpacing: 2, textTransform: 'uppercase' },
  preco: { fontFamily: fontes.monoMedia, fontSize: 15 },
  botao: { fontFamily: fontes.pesada, fontSize: 15, letterSpacing: 2, textTransform: 'uppercase' },
};

/** Altura mínima de áreas tocáveis (acessibilidade e zona do polegar). */
export const ALTURA_BOTAO = 56;
