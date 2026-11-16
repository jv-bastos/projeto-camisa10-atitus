import { Pressable, StyleSheet, Text } from 'react-native';

import { cores, espacos, fontes, raios } from '../theme';

/** Opção selecionável (filtros e escolha de categoria). Amarelo quando ativo. */
export default function Chip({ rotulo, ativo, onPress }) {
  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityState={{ selected: ativo }}
      style={({ pressed }) => [estilos.chip, ativo && estilos.ativo, pressed && !ativo && estilos.pressionado]}
    >
      <Text style={[estilos.texto, ativo && estilos.textoAtivo]}>{rotulo}</Text>
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  chip: {
    height: 36,
    paddingHorizontal: espacos.md - 2,
    borderRadius: raios.sm,
    borderWidth: 1,
    borderColor: cores.bordaForte,
    justifyContent: 'center',
  },
  ativo: { backgroundColor: cores.destaque, borderColor: cores.destaque },
  pressionado: { backgroundColor: cores.superficie },
  texto: { fontFamily: fontes.mono, fontSize: 12, letterSpacing: 1, textTransform: 'uppercase', color: cores.texto },
  textoAtivo: { fontFamily: fontes.monoMedia, color: cores.textoSobreDestaque },
});
