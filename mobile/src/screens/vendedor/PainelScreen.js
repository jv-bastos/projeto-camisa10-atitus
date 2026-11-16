import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useState } from 'react';
import { ActivityIndicator, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import Botao from '../../components/Botao';
import LinhasDoCampo from '../../components/LinhasDoCampo';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { useAuth } from '../../contexts/AuthContext';
import { mensagemDeErro } from '../../services/api';
import { buscarResumo, listarVendas } from '../../services/vendedorService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarData, formatarPreco } from '../../utils/formatar';

/** RF06 – painel do vendedor: faturamento, vendas, produtos ativos e últimas vendas. */
export default function PainelScreen({ navigation }) {
  const { usuario } = useAuth();
  const [resumo, setResumo] = useState(null);
  const [ultimas, setUltimas] = useState([]);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = useCallback(async () => {
    setErro('');
    try {
      const [dados, vendas] = await Promise.all([buscarResumo(), listarVendas()]);
      setResumo(dados);
      setUltimas(vendas.slice(0, 3));
    } catch (e) {
      setErro(mensagemDeErro(e));
    } finally {
      setAtualizando(false);
    }
  }, []);

  // recarrega sempre que o painel aparece (ex.: depois de cadastrar um produto)
  useFocusEffect(
    useCallback(() => {
      carregar();
    }, [carregar]),
  );

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <ScrollView
        contentContainerStyle={estilos.conteudo}
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
      >
        <TituloTela rotulo={usuario.nome} fino="Seu" forte="painel" />
        <MensagemErro mensagem={erro} />

        {!resumo && !erro ? <ActivityIndicator color={cores.destaque} style={estilos.carregando} /> : null}

        {resumo ? (
          <>
            <View style={estilos.placaPrincipal}>
              <LinhasDoCampo tamanho={300} top={-60} left={150} />
              <Text style={estilos.rotulo}>Faturamento</Text>
              <Text style={estilos.faturamento} adjustsFontSizeToFit numberOfLines={1}>
                {formatarPreco(resumo.faturamento)}
              </Text>
            </View>

            <View style={estilos.linhaPlacas}>
              <Placa rotulo="Vendas" valor={resumo.numeroVendas} />
              <Placa rotulo="Produtos ativos" valor={resumo.totalProdutos} />
            </View>

            <Botao
              titulo="Novo produto"
              icone="mais"
              variante="secundario"
              onPress={() => navigation.navigate('ProdutoForm')}
            />

            <View style={estilos.cabecalhoSecao}>
              <Text style={estilos.rotulo}>Últimas vendas</Text>
              {ultimas.length ? (
                <Pressable onPress={() => navigation.navigate('Vendas')} accessibilityRole="button" hitSlop={10}>
                  <Text style={estilos.link}>Ver todas</Text>
                </Pressable>
              ) : null}
            </View>

            {ultimas.length === 0 ? (
              <Text style={estilos.vazio}>Nenhuma venda ainda. Quando alguém comprar, ela aparece aqui.</Text>
            ) : (
              ultimas.map((v) => (
                <View key={v.pedidoId} style={estilos.venda}>
                  <View style={estilos.flex}>
                    <Text style={estilos.comprador} numberOfLines={1}>
                      {v.compradorNome}
                    </Text>
                    <Text style={estilos.data}>
                      Pedido nº {v.pedidoId} · {formatarData(v.dataCriacao)}
                    </Text>
                  </View>
                  <Text style={estilos.valorVenda}>{formatarPreco(v.valor)}</Text>
                </View>
              ))
            )}
          </>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function Placa({ rotulo, valor }) {
  return (
    <View style={estilos.placa}>
      <Text style={estilos.rotulo}>{rotulo}</Text>
      <Text style={estilos.numero}>{valor}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { padding: espacos.lg, gap: espacos.md },
  carregando: { marginTop: espacos.xxl },
  flex: { flex: 1 },
  placaPrincipal: {
    backgroundColor: cores.superficie,
    borderRadius: raios.md,
    padding: espacos.lg,
    gap: espacos.sm,
    overflow: 'hidden',
    marginTop: espacos.sm,
  },
  faturamento: { fontFamily: fontes.monoMedia, fontSize: 34, color: cores.destaque },
  linhaPlacas: { flexDirection: 'row', gap: espacos.md - 4 },
  placa: { flex: 1, backgroundColor: cores.superficie, borderRadius: raios.md, padding: espacos.md, gap: espacos.xs },
  numero: { fontFamily: fontes.pesada, fontSize: 40, letterSpacing: -2, color: cores.texto },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  cabecalhoSecao: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: espacos.md },
  link: { ...tipografia.rotulo, color: cores.destaque },
  vazio: { ...tipografia.corpo, fontSize: 15, color: cores.textoSecundario },
  venda: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.md,
    paddingVertical: espacos.md - 4,
    borderBottomWidth: 1,
    borderBottomColor: cores.divisoria,
  },
  comprador: { fontFamily: fontes.negrito, fontSize: 15, color: cores.texto },
  data: { ...tipografia.pequeno, color: cores.textoSecundario, marginTop: 2 },
  valorVenda: { ...tipografia.preco, color: cores.destaque },
});
