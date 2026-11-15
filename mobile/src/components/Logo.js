import { StyleSheet, Text, View } from 'react-native';

import { cores, espacos, fontes, tipografia } from '../theme';

/**
 * Marca do app.
 * - completo (login): slogan em mono, "Camisa" fino e um "10" enorme em amarelo.
 * - compacto (cabeçalhos): "Camisa 10" numa linha só.
 */
export default function Logo({ compacto = false }) {
  if (compacto) {
    return (
      <Text style={estilos.compacto} accessibilityLabel="Camisa 10">
        Camisa <Text style={estilos.compactoNumero}>10</Text>
      </Text>
    );
  }

  return (
    <View accessible accessibilityLabel="Camisa 10, a marca do boleiro">
      <Text style={estilos.slogan}>A marca do boleiro</Text>
      <Text style={estilos.camisa}>Camisa</Text>
      <Text style={estilos.numero}>10</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  slogan: { ...tipografia.rotulo, fontSize: 12, letterSpacing: 4, color: cores.destaque },
  camisa: {
    fontFamily: fontes.fina,
    fontSize: 64,
    lineHeight: 70,
    letterSpacing: -2,
    color: cores.texto,
    marginTop: espacos.sm,
  },
  // lineHeight MAIOR que o fontSize: com lineHeight menor, o iOS/Android cortam o topo do "10".
  // As margens negativas tiram o espaço extra que isso cria acima e abaixo.
  numero: {
    fontFamily: fontes.pesada,
    fontSize: 160,
    lineHeight: 184,
    letterSpacing: -10,
    color: cores.destaque,
    marginLeft: -6,
    marginTop: -18,
    marginBottom: -22,
  },
  compacto: { fontFamily: fontes.fina, fontSize: 26, letterSpacing: -1, color: cores.texto },
  compactoNumero: { fontFamily: fontes.pesada, color: cores.destaque },
});
