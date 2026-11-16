import { StyleSheet, Text, View } from 'react-native';

import { cores, espacos, tipografia } from '../theme';

/**
 * Título padrão das telas: rótulo pequeno em mono + título com a assinatura
 * do app (parte fina + parte pesada). Ex.: rotulo="Fala, Eduardo" fino="A" forte="vitrine".
 */
export default function TituloTela({ rotulo, fino, forte, style }) {
  return (
    <View style={[estilos.container, style]}>
      {rotulo ? <Text style={estilos.rotulo}>{rotulo}</Text> : null}
      <Text style={estilos.titulo} accessibilityRole="header">
        {fino ? `${fino} ` : ''}
        <Text style={estilos.forte}>{forte}</Text>
      </Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  container: { gap: espacos.xs },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  titulo: { ...tipografia.tituloFino, color: cores.texto },
  forte: { ...tipografia.tituloForte },
});
