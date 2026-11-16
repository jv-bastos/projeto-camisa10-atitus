import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useState } from 'react';
import { ActivityIndicator, FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import EstadoVazio from '../../components/EstadoVazio';
import FotoProduto from '../../components/FotoProduto';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { mensagemDeErro } from '../../services/api';
import { listarVendas } from '../../services/vendedorService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarData, formatarPreco, ROTULO_STATUS } from '../../utils/formatar';

/**
 * RF07 – vendas do vendedor: cada pedido que tem produtos dele, com comprador,
 * itens (e a personalização, quando houver) e o valor que coube a ele.
 */
export default function VendasScreen() {
  const [vendas, setVendas] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = useCallback(async () => {
    setErro('');
    try {
      setVendas(await listarVendas());
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setCarregando(false);
      setAtualizando(false);
    }
  }, []);

  useFocusEffect(
    useCallback(() => {
      carregar();
    }, [carregar]),
  );

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <FlatList
        data={vendas}
        keyExtractor={(v) => String(v.pedidoId)}
        contentContainerStyle={estilos.conteudo}
        ListHeaderComponent={
          <View style={estilos.cabecalho}>
            <TituloTela rotulo="Histórico" fino="Minhas" forte="vendas" />
            <MensagemErro mensagem={erro} />
          </View>
        }
        renderItem={({ item }) => <CartaoVenda venda={item} />}
        ItemSeparatorComponent={() => <View style={{ height: espacos.md - 4 }} />}
        ListEmptyComponent={
          carregando ? (
            <ActivityIndicator color={cores.destaque} style={estilos.carregando} />
          ) : erro ? null : (
            <EstadoVazio icone="vendas" titulo="Nenhuma venda ainda" texto="Quando alguém comprar suas camisas, a venda aparece aqui." />
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

function CartaoVenda({ venda }) {
  return (
    <View style={estilos.cartao}>
      <View style={estilos.topo}>
        <View style={estilos.flex}>
          <Text style={estilos.comprador} numberOfLines={1}>
            {venda.compradorNome}
          </Text>
          <Text style={estilos.data}>
            Pedido nº {venda.pedidoId} · {formatarData(venda.dataCriacao)}
          </Text>
        </View>
        <Text style={estilos.status}>{ROTULO_STATUS[venda.status] ?? venda.status}</Text>
      </View>

      {venda.itens.map((item) => (
        <View key={item.id} style={estilos.item}>
          <FotoProduto caminho={item.imagemUrl} style={estilos.miniatura} tamanhoIcone={20} />
          <View style={estilos.flex}>
            <Text style={estilos.nomeItem} numberOfLines={2}>
              <Text style={estilos.qtd}>{item.quantidade}× </Text>
              {item.produtoNome}
              {item.tamanho ? <Text style={estilos.qtd}> · {item.tamanho}</Text> : null}
            </Text>
            {item.personalizacao ? (
              <Text style={estilos.personalizacao}>
                Personalizada: {item.personalizacao.nome} · {item.personalizacao.numero}
              </Text>
            ) : null}
          </View>
          <Text style={estilos.subtotal}>{formatarPreco(item.subtotal)}</Text>
        </View>
      ))}

      <View style={estilos.rodape}>
        <Text style={estilos.rotulo}>Valor da venda</Text>
        <Text style={estilos.total}>{formatarPreco(venda.valor)}</Text>
      </View>
    </View>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { paddingHorizontal: espacos.lg, paddingBottom: espacos.xl },
  cabecalho: { paddingTop: espacos.lg, marginBottom: espacos.lg, gap: espacos.md },
  carregando: { marginTop: espacos.xxl },
  flex: { flex: 1 },
  cartao: { backgroundColor: cores.superficie, borderRadius: raios.md, padding: espacos.md },
  topo: { flexDirection: 'row', alignItems: 'flex-start', gap: espacos.sm, marginBottom: espacos.sm },
  comprador: { fontFamily: fontes.negrito, fontSize: 16, color: cores.texto },
  data: { ...tipografia.pequeno, color: cores.textoSecundario, marginTop: 2 },
  status: { ...tipografia.rotulo, fontSize: 10, color: cores.textoSecundario },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.sm + 2,
    paddingVertical: espacos.sm,
    borderTopWidth: 1,
    borderTopColor: cores.borda,
  },
  miniatura: { width: 40, height: 40, borderRadius: raios.sm },
  nomeItem: { ...tipografia.pequeno, fontSize: 14, color: cores.texto },
  qtd: { fontFamily: fontes.monoMedia, color: cores.textoSecundario },
  personalizacao: { ...tipografia.rotulo, fontSize: 10, color: cores.destaque, marginTop: 2 },
  subtotal: { ...tipografia.preco, fontSize: 13, color: cores.texto },
  rodape: {
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
