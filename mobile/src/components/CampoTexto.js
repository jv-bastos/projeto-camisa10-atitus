import { useState } from 'react';
import { StyleSheet, Text, TextInput, View } from 'react-native';

import { cores, espacos, tipografia } from '../theme';

/**
 * Campo de formulário "sublinhado": rótulo em fonte mono, linha amarela no foco
 * e vermelha quando há erro. Repassa as demais props para o TextInput.
 * "ref" (React 19 permite recebê-lo como prop) serve para focar o campo via código.
 * "contador" (ex.: "7/12") aparece à direita do rótulo.
 */
export default function CampoTexto({ rotulo, erro, contador, style, ref, ...propsDoInput }) {
  const [focado, setFocado] = useState(false);

  return (
    <View style={[estilos.container, style]}>
      {rotulo ? (
        <View style={estilos.linhaRotulo}>
          <Text style={estilos.rotulo}>{rotulo}</Text>
          {contador ? <Text style={estilos.rotulo}>{contador}</Text> : null}
        </View>
      ) : null}
      <TextInput
        ref={ref}
        placeholderTextColor={cores.textoSecundario}
        selectionColor={cores.destaque}
        cursorColor={cores.destaque}
        {...propsDoInput}
        onFocus={(e) => {
          setFocado(true);
          propsDoInput.onFocus?.(e);
        }}
        onBlur={(e) => {
          setFocado(false);
          propsDoInput.onBlur?.(e);
        }}
        style={[estilos.input, focado && estilos.inputFocado, erro && estilos.inputComErro]}
        accessibilityLabel={rotulo}
      />
      {erro ? <Text style={estilos.erro}>{erro}</Text> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  container: { marginBottom: espacos.lg },
  linhaRotulo: { flexDirection: 'row', justifyContent: 'space-between' },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  input: {
    ...tipografia.corpo,
    fontSize: 17,
    color: cores.texto,
    borderBottomWidth: 2,
    borderBottomColor: cores.bordaForte,
    paddingVertical: espacos.sm,
    minHeight: 48,
  },
  inputFocado: { borderBottomColor: cores.destaque },
  inputComErro: { borderBottomColor: cores.erro },
  erro: { ...tipografia.pequeno, color: cores.erro, marginTop: espacos.xs },
});
