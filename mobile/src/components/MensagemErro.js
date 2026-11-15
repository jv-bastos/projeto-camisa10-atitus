import { StyleSheet, Text, View } from 'react-native';

import { cores, espacos, raios, tipografia } from '../theme';

/** Faixa de erro geral (ex.: "E-mail ou senha inválidos."). Não renderiza nada sem mensagem. */
export default function MensagemErro({ mensagem }) {
  if (!mensagem) {
    return null;
  }
  return (
    <View style={estilos.caixa} accessibilityRole="alert">
      <Text style={estilos.texto}>{mensagem}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: {
    backgroundColor: 'rgba(242, 107, 107, 0.12)',
    borderWidth: 1,
    borderColor: cores.erro,
    borderRadius: raios.sm,
    padding: espacos.md,
    marginBottom: espacos.lg,
  },
  texto: { ...tipografia.pequeno, fontSize: 14, color: cores.texto },
});
