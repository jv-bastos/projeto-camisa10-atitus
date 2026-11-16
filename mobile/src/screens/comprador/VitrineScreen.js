import { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  FlatList,
  Pressable,
  RefreshControl,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import Botao from '../../components/Botao';
import CampoBusca from '../../components/CampoBusca';
import CartaoProduto from '../../components/CartaoProduto';
import Chip from '../../components/Chip';
import EstadoVazio from '../../components/EstadoVazio';
import FiltrosVitrine, { contarFiltrosAtivos, FILTROS_VAZIOS, filtrosParaApi } from '../../components/FiltrosVitrine';
import Icone from '../../components/Icone';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { useAuth } from '../../contexts/AuthContext';
import { mensagemDeErro } from '../../services/api';
import { buscarOpcoesFiltro, listarCategorias, listarProdutos } from '../../services/catalogoService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';

const ESPERA_BUSCA_MS = 400;

/**
 * RF09/RF10 – vitrine do comprador: busca por nome, filtro por categoria
 * e grade de camisas. Tocar numa camisa abre o detalhe (RF11).
 */
export default function VitrineScreen({ navigation }) {
  const { usuario } = useAuth();
  const primeiroNome = usuario.nome.trim().split(/\s+/)[0];

  const [categorias, setCategorias] = useState([]);
  const [categoriaId, setCategoriaId] = useState(null);
  const [busca, setBusca] = useState('');
  const [produtos, setProdutos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [atualizando, setAtualizando] = useState(false);
  const [erro, setErro] = useState('');
  const [filtros, setFiltros] = useState(FILTROS_VAZIOS);
  const [opcoesFiltro, setOpcoesFiltro] = useState(null);
  const [painelAberto, setPainelAberto] = useState(false);
  const filtrosAtivos = contarFiltrosAtivos(filtros);

  // evita que uma resposta antiga (de uma busca anterior) sobrescreva a mais nova
  const ultimaRequisicao = useRef(0);

  const carregarProdutos = useCallback(async () => {
    const numero = ++ultimaRequisicao.current;
    setErro('');
    try {
      const lista = await listarProdutos({ ...filtrosParaApi(filtros), categoriaId, busca });
      if (numero === ultimaRequisicao.current) setProdutos(lista);
    } catch (e) {
      if (numero === ultimaRequisicao.current) setErro(mensagemDeErro(e));
    } finally {
      if (numero === ultimaRequisicao.current) {
        setCarregando(false);
        setAtualizando(false);
      }
    }
  }, [categoriaId, busca, filtros]);

  useEffect(() => {
    listarCategorias()
      .then(setCategorias)
      .catch(() => {}); // sem categorias a vitrine continua funcionando, só sem os filtros
    buscarOpcoesFiltro()
      .then(setOpcoesFiltro)
      .catch(() => {});
  }, []);

  // recarrega ao trocar a categoria (na hora) ou a busca (depois de uma pausa na digitação)
  useEffect(() => {
    setCarregando(true);
    const temporizador = setTimeout(carregarProdutos, busca ? ESPERA_BUSCA_MS : 0);
    return () => clearTimeout(temporizador);
  }, [carregarProdutos, busca]);

  function aoPuxarParaAtualizar() {
    setAtualizando(true);
    carregarProdutos();
  }

  const cabecalho = (
    <View style={estilos.cabecalho}>
      <TituloTela rotulo={`Fala, ${primeiroNome}`} fino="A" forte="vitrine" />

      <View style={estilos.linhaBusca}>
        <View style={estilos.flex}>
          <CampoBusca valor={busca} onChange={setBusca} placeholder="Buscar time ou seleção" rotuloAcessivel="Buscar camisas" />
        </View>
        <Pressable
          onPress={() => setPainelAberto(true)}
          accessibilityRole="button"
          accessibilityLabel={`Filtrar e ordenar${filtrosAtivos ? `, ${filtrosAtivos} filtros ativos` : ''}`}
          style={[estilos.botaoFiltro, filtrosAtivos > 0 && estilos.botaoFiltroAtivo]}
        >
          <Icone nome="filtro" cor={filtrosAtivos ? cores.textoSobreDestaque : cores.texto} tamanho={22} espessura={1.8} />
          {filtrosAtivos ? (
            <View style={estilos.contador}>
              <Text style={estilos.textoContador}>{filtrosAtivos}</Text>
            </View>
          ) : null}
        </Pressable>
      </View>
      {filtros.ordenar !== 'nome' && opcoesFiltro ? (
        <Text style={estilos.ordenacaoAtual}>
          Ordenado por: {opcoesFiltro.ordenacoes.find((o) => o.valor === filtros.ordenar)?.rotulo}
        </Text>
      ) : null}

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={estilos.chips}
        style={estilos.faixaChips}
        keyboardShouldPersistTaps="handled"
      >
        <Chip rotulo="Todas" ativo={categoriaId === null} onPress={() => setCategoriaId(null)} />
        {categorias.map((c) => (
          <Chip key={c.id} rotulo={c.nome} ativo={categoriaId === c.id} onPress={() => setCategoriaId(c.id)} />
        ))}
      </ScrollView>

      {erro ? (
        <View>
          <MensagemErro mensagem={erro} />
          <Botao titulo="Tentar de novo" variante="secundario" onPress={carregarProdutos} />
        </View>
      ) : null}
    </View>
  );

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <FlatList
        data={erro ? [] : produtos}
        keyExtractor={(p) => String(p.id)}
        numColumns={2}
        columnWrapperStyle={estilos.linhaGrade}
        contentContainerStyle={estilos.conteudo}
        ListHeaderComponent={cabecalho}
        keyboardShouldPersistTaps="handled"
        keyboardDismissMode="on-drag"
        renderItem={({ item }) => (
          <View style={estilos.celula}>
            <CartaoProduto
              produto={item}
              onPress={() => navigation.navigate('Produto', { produtoId: item.id, produto: item })}
            />
          </View>
        )}
        ListEmptyComponent={
          carregando ? (
            <ActivityIndicator color={cores.destaque} style={estilos.carregando} />
          ) : erro ? null : (
            <EstadoVazio
              icone="busca"
              titulo="Nenhuma camisa encontrada"
              texto="Tente outro nome, outra categoria ou menos filtros."
              acao={filtrosAtivos ? 'Limpar filtros' : undefined}
              onAcao={() => setFiltros(FILTROS_VAZIOS)}
            />
          )
        }
        refreshControl={
          <RefreshControl
            refreshing={atualizando}
            onRefresh={aoPuxarParaAtualizar}
            tintColor={cores.destaque}
            colors={[cores.destaque]}
            progressBackgroundColor={cores.superficie}
          />
        }
      />
      <FiltrosVitrine
        visivel={painelAberto}
        opcoes={opcoesFiltro}
        valores={filtros}
        onAplicar={(novos) => {
          setFiltros(novos);
          setPainelAberto(false);
        }}
        onFechar={() => setPainelAberto(false)}
      />
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  conteudo: { paddingHorizontal: espacos.md + 4, paddingBottom: espacos.xl },
  cabecalho: { paddingTop: espacos.lg, gap: espacos.md, marginBottom: espacos.md },
  flex: { flex: 1 },
  linhaBusca: { flexDirection: 'row', gap: espacos.sm },
  botaoFiltro: {
    width: 48,
    height: 48,
    borderRadius: raios.sm,
    borderWidth: 1,
    borderColor: cores.borda,
    backgroundColor: cores.superficie,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoFiltroAtivo: { backgroundColor: cores.destaque, borderColor: cores.destaque },
  contador: {
    position: 'absolute',
    top: -6,
    right: -6,
    minWidth: 20,
    height: 20,
    borderRadius: raios.redondo,
    backgroundColor: cores.texto,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 4,
  },
  textoContador: { fontFamily: fontes.pesada, fontSize: 11, color: cores.fundo },
  ordenacaoAtual: { ...tipografia.rotulo, color: cores.destaque, marginTop: -espacos.sm },
  faixaChips: { marginHorizontal: -(espacos.md + 4) },
  chips: { gap: espacos.sm, paddingHorizontal: espacos.md + 4 },
  linhaGrade: { gap: espacos.md - 4 },
  celula: { flex: 1, maxWidth: '50%', marginBottom: espacos.md - 4 },
  carregando: { marginTop: espacos.xxl },
});
