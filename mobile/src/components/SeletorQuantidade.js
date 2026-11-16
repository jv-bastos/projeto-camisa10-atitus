import { Pressable, StyleSheet, Text, View } from 'react-native';

import { cores, fontes, raios } from '../theme';
import Icone from './Icone';

/** Botões − e + com o número no meio. Respeita mínimo e máximo (ex.: estoque). */
export default function SeletorQuantidade({ valor, onChange, minimo = 1, maximo = 99, compacto = false }) {
  return (
    <View style={estilos.linha}>
      <BotaoQuantidade
        icone="menos"
        rotulo="Diminuir quantidade"
        compacto={compacto}
        desativado={valor <= minimo}
        onPress={() => onChange(valor - 1)}
      />
      <Text style={[estilos.valor, compacto && estilos.valorCompacto]} accessibilityLabel={`Quantidade ${valor}`}>
        {valor}
      </Text>
      <BotaoQuantidade
        icone="mais"
        rotulo="Aumentar quantidade"
        compacto={compacto}
        desativado={valor >= maximo}
        onPress={() => onChange(valor + 1)}
      />
    </View>
  );
}

function BotaoQuantidade({ icone, rotulo, compacto, desativado, onPress }) {
  const tamanho = compacto ? 36 : 44;
  return (
    <Pressable
      onPress={onPress}
      disabled={desativado}
      accessibilityRole="button"
      accessibilityLabel={rotulo}
      accessibilityState={{ disabled: desativado }}
      hitSlop={compacto ? 4 : 0}
      style={({ pressed }) => [
        estilos.botao,
        { width: tamanho, height: tamanho },
        pressed && estilos.pressionado,
        desativado && estilos.desativado,
      ]}
    >
      <Icone nome={icone} cor={cores.texto} tamanho={compacto ? 16 : 18} espessura={2} />
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  linha: { flexDirection: 'row', alignItems: 'center' },
  botao: {
    borderWidth: 1,
    borderColor: cores.bordaForte,
    borderRadius: raios.sm,
    alignItems: 'center',
    justifyContent: 'center',
  },
  pressionado: { backgroundColor: cores.superficieAlta },
  desativado: { opacity: 0.35 },
  valor: { fontFamily: fontes.monoMedia, fontSize: 18, color: cores.texto, minWidth: 44, textAlign: 'center' },
  valorCompacto: { fontSize: 15, minWidth: 34 },
});
