import Svg, { Path, Text as SvgText } from 'react-native-svg';

import { fontes } from '../theme';

const CORPO =
  'M66 18 L40 28 L10 58 L32 90 L50 80 L50 196 Q100 204 150 196 L150 80 L168 90 L190 58 L160 28 L134 18 Q100 34 66 18 Z';
const GOLA = 'M66 18 Q100 30 134 18';

/**
 * Desenho de camisa nas cores do produto: corpo em "corPrincipal" e o número
 * em "corTexto". Usado na revisão do cadastro para conferir a combinação de cores.
 */
export default function CamisaDesenho({ corPrincipal, corTexto, numero = '10', tamanho = 120 }) {
  return (
    <Svg width={tamanho} height={tamanho * 1.05} viewBox="0 0 200 210" accessibilityLabel="Desenho da camisa com as cores escolhidas">
      <Path d={CORPO} fill={corPrincipal} stroke="rgba(0,0,0,0.35)" strokeWidth={3} strokeLinejoin="round" />
      <Path d={GOLA} fill="none" stroke={corTexto} strokeWidth={6} />
      <SvgText x="100" y="150" textAnchor="middle" fontSize="72" fontFamily={fontes.pesada} fill={corTexto}>
        {numero}
      </SvgText>
    </Svg>
  );
}
