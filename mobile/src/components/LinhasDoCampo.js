import { StyleSheet, View } from 'react-native';
import Svg, { Circle, Line } from 'react-native-svg';

import { cores } from '../theme';

/**
 * Decoração de fundo: círculo central e linha do meio-campo, em cal bem apagada.
 * Fica atrás do conteúdo e não recebe toques.
 */
export default function LinhasDoCampo({ tamanho = 460, top = -40, left = -50 }) {
  const centro = tamanho / 2;
  return (
    <View pointerEvents="none" style={[estilos.camada, { top, left }]}>
      <Svg width={tamanho} height={tamanho}>
        <Circle cx={centro} cy={centro} r={tamanho * 0.326} stroke={cores.cal} strokeOpacity={0.1} strokeWidth={1.5} fill="none" />
        <Line x1={0} y1={centro} x2={tamanho} y2={centro} stroke={cores.cal} strokeOpacity={0.1} strokeWidth={1.5} />
        <Circle cx={centro} cy={centro} r={4} fill={cores.cal} fillOpacity={0.2} />
      </Svg>
    </View>
  );
}

const estilos = StyleSheet.create({
  camada: { position: 'absolute' },
});
