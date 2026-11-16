import { Pressable, StyleSheet, Text, View } from 'react-native';

import { cores, espacos, fontes, raios, tipografia } from '../theme';
import { formatarPreco } from '../utils/formatar';
import FotoProduto from './FotoProduto';

/** Cartão da vitrine: foto, nome e preço. Avisa quando está esgotada ou restam poucas unidades. */
export default function CartaoProduto({ produto, onPress }) {
  const esgotada = produto.estoque === 0;
  const poucas = produto.estoque > 0 && produto.estoque <= 5;

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`${produto.nome}, ${formatarPreco(produto.preco)}${esgotada ? ', esgotada' : ''}`}
      style={({ pressed }) => [estilos.cartao, pressed && estilos.pressionado]}
    >
      <FotoProduto caminho={produto.imagemUrl} style={[estilos.foto, esgotada && estilos.fotoEsgotada]} />
      {esgotada ? (
        <View style={[estilos.selo, estilos.seloEsgotado]}>
          <Text style={[estilos.textoSelo, estilos.textoEsgotado]}>Esgotada</Text>
        </View>
      ) : poucas ? (
        <View style={estilos.selo}>
          <Text style={estilos.textoSelo}>Últimas {produto.estoque}</Text>
        </View>
      ) : null}
      <View style={estilos.info}>
        <Text style={estilos.nome} numberOfLines={2}>
          {produto.nome.replace(/^Camisa /, '')}
        </Text>
        <Text style={[estilos.preco, esgotada && estilos.precoEsgotado]}>{formatarPreco(produto.preco)}</Text>
      </View>
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  cartao: {
    flex: 1,
    backgroundColor: cores.superficie,
    borderRadius: raios.md,
    overflow: 'hidden',
  },
  pressionado: { opacity: 0.85 },
  foto: { width: '100%', aspectRatio: 1, borderBottomWidth: 1, borderBottomColor: cores.borda },
  selo: {
    position: 'absolute',
    top: espacos.sm,
    left: espacos.sm,
    backgroundColor: cores.fundo,
    borderRadius: raios.sm,
    paddingHorizontal: 6,
    paddingVertical: 3,
  },
  textoSelo: { ...tipografia.rotulo, fontSize: 9, letterSpacing: 1, color: cores.destaque },
  fotoEsgotada: { opacity: 0.45 },
  seloEsgotado: { backgroundColor: cores.erro },
  textoEsgotado: { fontFamily: fontes.monoMedia, color: cores.textoSobreDestaque },
  precoEsgotado: { color: cores.textoSecundario, textDecorationLine: 'line-through' },
  info: { padding: espacos.sm + 4, gap: espacos.xs },
  nome: { fontFamily: fontes.negrito, fontSize: 14, lineHeight: 18, color: cores.texto, minHeight: 36 },
  preco: { ...tipografia.preco, fontSize: 14, color: cores.destaque },
});
