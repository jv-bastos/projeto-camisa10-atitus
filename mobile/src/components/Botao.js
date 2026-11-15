import { ActivityIndicator, Pressable, StyleSheet, Text, View } from 'react-native';

import { ALTURA_BOTAO, cores, espacos, fontes, raios, tipografia } from '../theme';
import Icone from './Icone';

/** Cor do texto/ícone de cada variante. */
const COR_CONTEUDO = {
  primario: cores.textoSobreDestaque,
  secundario: cores.texto,
  perigo: cores.erro,
  link: cores.destaque,
};

/**
 * Botão padrão do app.
 * variante: 'primario' (amarelo, ação principal) | 'secundario' (contorno) | 'perigo' | 'link' (só texto)
 * Enquanto "carregando" for true, mostra um indicador e não aceita toques (evita envio duplo).
 */
export default function Botao({
  titulo,
  onPress,
  variante = 'primario',
  carregando = false,
  desabilitado = false,
  icone,
  style,
}) {
  const inativo = desabilitado || carregando;
  const corConteudo = COR_CONTEUDO[variante];

  return (
    <Pressable
      onPress={onPress}
      disabled={inativo}
      accessibilityRole="button"
      accessibilityState={{ disabled: inativo, busy: carregando }}
      style={({ pressed }) => [
        estilos.base,
        estilos[variante],
        pressed && estilos.pressionado,
        inativo && estilos.inativo,
        style,
      ]}
    >
      {carregando ? (
        <ActivityIndicator color={corConteudo} />
      ) : (
        <View style={estilos.conteudo}>
          {icone ? <Icone nome={icone} cor={corConteudo} tamanho={20} espessura={2} /> : null}
          <Text style={[variante === 'link' ? estilos.legendaLink : estilos.legenda, { color: corConteudo }]}>
            {titulo}
          </Text>
        </View>
      )}
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  base: {
    minHeight: ALTURA_BOTAO,
    borderRadius: raios.sm,
    paddingHorizontal: espacos.lg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  primario: { backgroundColor: cores.destaque },
  secundario: { borderWidth: 1, borderColor: cores.bordaForte },
  perigo: { borderWidth: 1, borderColor: cores.erro },
  link: { minHeight: 44 },
  pressionado: { opacity: 0.85, transform: [{ scale: 0.98 }] },
  inativo: { opacity: 0.5 },
  conteudo: { flexDirection: 'row', alignItems: 'center', gap: espacos.sm },
  legenda: { ...tipografia.botao },
  legendaLink: { fontFamily: fontes.negrito, fontSize: 15 },
});
