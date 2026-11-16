import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { cores, espacos, raios, tipografia } from '../theme';
import Icone from './Icone';

/** Caixa de busca com lupa e botão "Limpar" (vitrine e catálogo de fotos). */
export default function CampoBusca({ valor, onChange, placeholder, rotuloAcessivel }) {
  return (
    <View style={estilos.caixa}>
      <Icone nome="busca" cor={cores.textoSecundario} tamanho={20} espessura={1.8} />
      <TextInput
        value={valor}
        onChangeText={onChange}
        placeholder={placeholder}
        placeholderTextColor={cores.textoSecundario}
        selectionColor={cores.destaque}
        cursorColor={cores.destaque}
        style={estilos.input}
        returnKeyType="search"
        autoCorrect={false}
        accessibilityLabel={rotuloAcessivel ?? placeholder}
      />
      {valor ? (
        <Pressable onPress={() => onChange('')} accessibilityRole="button" accessibilityLabel="Limpar busca" hitSlop={10}>
          <Text style={estilos.limpar}>Limpar</Text>
        </Pressable>
      ) : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  caixa: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.sm + 2,
    height: 48,
    paddingHorizontal: espacos.md - 2,
    backgroundColor: cores.superficie,
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: raios.sm,
  },
  input: { flex: 1, ...tipografia.corpo, fontSize: 15, color: cores.texto, paddingVertical: 0 },
  limpar: { ...tipografia.rotulo, color: cores.destaque },
});
