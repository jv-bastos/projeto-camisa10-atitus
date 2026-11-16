import { StyleSheet, Text, View } from 'react-native';

import { cores, espacos, tipografia } from '../theme';
import Botao from './Botao';
import Icone from './Icone';

/** Mensagem para listas vazias (carrinho, pedidos, busca sem resultado), com ação opcional. */
export default function EstadoVazio({ icone = 'camisa', titulo, texto, acao, onAcao }) {
  return (
    <View style={estilos.container}>
      <Icone nome={icone} cor={cores.bordaForte} tamanho={56} espessura={1.4} />
      <Text style={estilos.titulo}>{titulo}</Text>
      {texto ? <Text style={estilos.texto}>{texto}</Text> : null}
      {acao ? <Botao titulo={acao} variante="secundario" onPress={onAcao} style={estilos.botao} /> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  container: { alignItems: 'center', paddingVertical: espacos.xxl, paddingHorizontal: espacos.lg, gap: espacos.sm },
  titulo: { ...tipografia.subtitulo, color: cores.texto, marginTop: espacos.sm, textAlign: 'center' },
  texto: { ...tipografia.corpo, fontSize: 15, color: cores.textoSecundario, textAlign: 'center' },
  botao: { marginTop: espacos.md, alignSelf: 'stretch' },
});
