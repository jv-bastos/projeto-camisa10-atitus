import { useFocusEffect } from '@react-navigation/native';
import { useCallback, useState } from 'react';
import { ActivityIndicator, Alert, FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import Botao from '../../components/Botao';
import EstadoVazio from '../../components/EstadoVazio';
import FotoProduto from '../../components/FotoProduto';
import Icone from '../../components/Icone';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { mensagemDeErro } from '../../services/api';
import { excluirProduto, listarMeusProdutos } from '../../services/vendedorService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarPreco } from '../../utils/formatar';

/** RF04/RF08 – produtos do vendedor: tocar para editar, lixeira para excluir, botão para cadastrar. */
export default function MeusProdutosScreen({ navigation }) {
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = useCallback(async () => {
    setErro('');
    try {
      setProdutos(await listarMeusProdutos());
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

  function confirmarExclusao(produto) {
    Alert.alert(
      'Excluir produto?',
      `"${produto.nome}" sai da vitrine. Os pedidos antigos continuam registrados.`,
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Excluir',
          style: 'destructive',
          onPress: async () => {
            try {
              await excluirProduto(produto.id);
              setProdutos((atuais) => atuais.filter((p) => p.id !== produto.id));
            } catch (e) {
              Alert.alert('Não foi possível excluir', mensagemDeErro(e));
            }
          },
        },
      ],
    );
  }

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <FlatList
        data={produtos}
        keyExtractor={(p) => String(p.id)}
        contentContainerStyle={estilos.conteudo}
        ListHeaderComponent={
          <View style={estilos.cabecalho}>
            <TituloTela
              rotulo={`${produtos.length} ${produtos.length === 1 ? 'produto ativo' : 'produtos ativos'}`}
              fino="Meus"
              forte="produtos"
            />
            <MensagemErro mensagem={erro} />
          </View>
        }
        renderItem={({ item }) => (
          <Pressable
            onPress={() => navigation.navigate('ProdutoForm', { produto: item })}
            accessibilityRole="button"
            accessibilityLabel={`Editar ${item.nome}`}
            style={({ pressed }) => [estilos.item, pressed && estilos.pressionado]}
          >
            <FotoProduto caminho={item.imagemUrl} style={estilos.foto} tamanhoIcone={26} />
            <View style={estilos.info}>
              <Text style={estilos.nome} numberOfLines={2}>
                {item.nome}
              </Text>
              <Text style={estilos.categoria} numberOfLines={1}>
                {item.categorias?.map((c) => c.nome).join(' · ')}
              </Text>
              <View style={estilos.linha}>
                <Text style={estilos.preco}>{formatarPreco(item.preco)}</Text>
                <Text style={[estilos.estoque, item.estoque === 0 && estilos.esgotado]}>
                  {item.estoque === 0 ? 'Esgotado' : `Estoque ${item.estoque}`}
                </Text>
              </View>
            </View>
            <Pressable
              onPress={() => confirmarExclusao(item)}
              accessibilityRole="button"
              accessibilityLabel={`Excluir ${item.nome}`}
              hitSlop={8}
              style={estilos.lixeira}
            >
              <Icone nome="lixeira" cor={cores.textoSecundario} tamanho={20} espessura={1.8} />
            </Pressable>
          </Pressable>
        )}
        ListEmptyComponent={
          carregando ? (
            <ActivityIndicator color={cores.destaque} style={estilos.carregando} />
          ) : erro ? null : (
            <EstadoVazio
              icone="camisa"
              titulo="Nenhum produto cadastrado"
              texto="Cadastre sua primeira camisa para ela aparecer na vitrine."
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
      <View style={estilos.rodape}>
        <Botao titulo="Novo produto" icone="mais" onPress={() => navigation.navigate('ProdutoForm')} />
      </View>
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { paddingHorizontal: espacos.lg, paddingBottom: espacos.lg },
  cabecalho: { paddingTop: espacos.lg, marginBottom: espacos.sm, gap: espacos.md },
  carregando: { marginTop: espacos.xxl },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.md - 2,
    paddingVertical: espacos.md - 4,
    borderBottomWidth: 1,
    borderBottomColor: cores.divisoria,
  },
  pressionado: { backgroundColor: cores.superficie },
  foto: { width: 64, height: 64, borderRadius: raios.md },
  info: { flex: 1, gap: 2 },
  nome: { fontFamily: fontes.negrito, fontSize: 15, lineHeight: 20, color: cores.texto },
  categoria: { ...tipografia.rotulo, fontSize: 10, color: cores.textoSecundario },
  linha: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 2 },
  preco: { ...tipografia.preco, fontSize: 14, color: cores.destaque },
  estoque: { ...tipografia.pequeno, color: cores.textoSecundario },
  esgotado: { color: cores.erro },
  lixeira: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center' },
  rodape: {
    paddingHorizontal: espacos.lg,
    paddingVertical: espacos.md - 4,
    backgroundColor: cores.barra,
    borderTopWidth: 1,
    borderTopColor: cores.divisoria,
  },
});
