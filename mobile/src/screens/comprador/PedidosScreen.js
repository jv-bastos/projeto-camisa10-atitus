import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useState } from 'react';
import { ActivityIndicator, FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import EstadoVazio from '../../components/EstadoVazio';
import FotoProduto from '../../components/FotoProduto';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { mensagemDeErro } from '../../services/api';
import { listarMeusPedidos } from '../../services/pedidoService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarData, formatarPreco, ROTULO_STATUS } from '../../utils/formatar';

const COR_STATUS = {
  CONCLUIDO: cores.textoSecundario,
  ENVIADO: cores.destaque,
  ENTREGUE: cores.sucesso,
};

/** RF14 – histórico de pedidos do comprador, do mais recente para o mais antigo. */
export default function PedidosScreen({ navigation }) {
  const [pedidos, setPedidos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = useCallback(async () => {
    setErro('');
    try {
      setPedidos(await listarMeusPedidos());
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setCarregando(false);
      setAtualizando(false);
    }
  }, []);

  // recarrega toda vez que a aba aparece (ex.: logo depois de finalizar uma compra)
  useFocusEffect(
    useCallback(() => {
      carregar();
    }, [carregar]),
  );

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <FlatList
        data={pedidos}
        keyExtractor={(p) => String(p.id)}
        contentContainerStyle={estilos.conteudo}
        ListHeaderComponent={
          <View style={estilos.cabecalho}>
            <TituloTela rotulo="Histórico" fino="Meus" forte="pedidos" />
            <MensagemErro mensagem={erro} />
          </View>
        }
        renderItem={({ item }) => <CartaoPedido pedido={item} />}
        ItemSeparatorComponent={() => <View style={{ height: espacos.md - 4 }} />}
        ListEmptyComponent={
          carregando ? (
            <ActivityIndicator color={cores.destaque} style={estilos.carregando} />
          ) : erro ? null : (
            <EstadoVazio
              icone="pedidos"
              titulo="Você ainda não fez pedidos"
              texto="Quando finalizar uma compra, ela aparece aqui."
              acao="Ver a vitrine"
              onAcao={() => navigation.navigate('Inicio')}
            />
          )
        }
        refreshControl={
          <RefreshControl
            refreshing={atualizando}
            onRefresh={() => {
              setAtualizando(true);
              carregar();
            }}
            tintColor={cores.destaque}
            colors={[cores.destaque]}
            progressBackgroundColor={cores.superficie}
          />
        }
      />
    </SafeAreaView>
  );
}

function CartaoPedido({ pedido }) {
  const status = {
    rotulo: ROTULO_STATUS[pedido.status] ?? pedido.status,
    cor: COR_STATUS[pedido.status] ?? cores.textoSecundario,
  };

  return (
    <View style={estilos.cartao}>
      <View style={estilos.topoCartao}>
        <View>
          <Text style={estilos.numero}>Pedido nº {pedido.id}</Text>
          <Text style={estilos.data}>{formatarData(pedido.dataCriacao)}</Text>
        </View>
        <View style={[estilos.status, { borderColor: status.cor }]}>
          <Text style={[estilos.textoStatus, { color: status.cor }]}>{status.rotulo}</Text>
        </View>
      </View>

      {pedido.itens.map((item) => (
        <View key={item.id} style={estilos.item}>
          <FotoProduto caminho={item.imagemUrl} style={estilos.miniatura} tamanhoIcone={20} />
          <Text style={estilos.nomeItem} numberOfLines={2}>
            <Text style={estilos.qtd}>{item.quantidade}× </Text>
            {item.produtoNome}
            {item.tamanho ? <Text style={estilos.qtd}> · {item.tamanho}</Text> : null}
          </Text>
          <Text style={estilos.subtotal}>{formatarPreco(item.subtotal)}</Text>
        </View>
      ))}

      <View style={estilos.rodapeCartao}>
        <Text style={estilos.rotulo}>Total</Text>
        <Text style={estilos.total}>{formatarPreco(pedido.valorTotal)}</Text>
      </View>
    </View>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { paddingHorizontal: espacos.lg, paddingBottom: espacos.xl },
  cabecalho: { paddingTop: espacos.lg, marginBottom: espacos.lg, gap: espacos.md },
  carregando: { marginTop: espacos.xxl },
  cartao: { backgroundColor: cores.superficie, borderRadius: raios.md, padding: espacos.md },
  topoCartao: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: espacos.sm },
  numero: { fontFamily: fontes.negrito, fontSize: 16, color: cores.texto },
  data: { ...tipografia.rotulo, color: cores.textoSecundario, marginTop: 2, textTransform: 'none', letterSpacing: 0.5 },
  status: { borderWidth: 1, borderRadius: raios.sm, paddingHorizontal: espacos.sm, paddingVertical: 3 },
  textoStatus: { ...tipografia.rotulo, fontSize: 10, letterSpacing: 1 },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.sm + 2,
    paddingVertical: espacos.sm,
    borderTopWidth: 1,
    borderTopColor: cores.borda,
  },
  miniatura: { width: 40, height: 40, borderRadius: raios.sm },
  nomeItem: { flex: 1, ...tipografia.pequeno, fontSize: 14, color: cores.texto },
  qtd: { fontFamily: fontes.monoMedia, color: cores.textoSecundario },
  subtotal: { ...tipografia.preco, fontSize: 13, color: cores.texto },
  rodapeCartao: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    paddingTop: espacos.sm + 2,
    borderTopWidth: 1,
    borderTopColor: cores.borda,
  },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  total: { ...tipografia.preco, fontSize: 17, color: cores.destaque },
});
