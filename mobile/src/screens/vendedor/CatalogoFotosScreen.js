import { useEffect, useRef, useState } from 'react';
import { ActivityIndicator, FlatList, Image, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import CampoBusca from '../../components/CampoBusca';
import Chip from '../../components/Chip';
import EstadoVazio from '../../components/EstadoVazio';
import Icone from '../../components/Icone';
import MensagemErro from '../../components/MensagemErro';
import TituloTela from '../../components/TituloTela';
import { mensagemDeErro, urlDaImagem } from '../../services/api';
import { listarCategorias } from '../../services/catalogoService';
import { listarFotosCatalogo } from '../../services/vendedorService';
import { cores, espacos, fontes, raios } from '../../theme';

const ESPERA_BUSCA_MS = 400;
const COLUNAS = 3;

/**
 * Catálogo de fotos do projeto: o vendedor escolhe a foto de uma camisa em vez
 * de fotografá-la. Ao tocar numa foto, volta para o formulário com ela escolhida
 * (o formulário usa o nome e as categorias sugeridos).
 */
export default function CatalogoFotosScreen({ navigation }) {
  const [categorias, setCategorias] = useState([]);
  const [categoriaId, setCategoriaId] = useState(null);
  const [busca, setBusca] = useState('');
  const [fotos, setFotos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const ultimaRequisicao = useRef(0);

  useEffect(() => {
    listarCategorias().then(setCategorias).catch(() => {});
  }, []);

  useEffect(() => {
    setCarregando(true);
    const numero = ++ultimaRequisicao.current;
    const temporizador = setTimeout(async () => {
      setErro('');
      try {
        const lista = await listarFotosCatalogo({ categoriaId, busca });
        if (numero === ultimaRequisicao.current) setFotos(lista);
      } catch (e) {
        if (numero === ultimaRequisicao.current) setErro(mensagemDeErro(e));
      } finally {
        if (numero === ultimaRequisicao.current) setCarregando(false);
      }
    }, busca ? ESPERA_BUSCA_MS : 0);
    return () => clearTimeout(temporizador);
  }, [categoriaId, busca]);

  function escolher(foto) {
    navigation.popTo('ProdutoForm', { fotoCatalogo: foto }, { merge: true });
  }

  return (
    <SafeAreaView style={estilos.tela} edges={['top']}>
      <View style={estilos.cabecalho}>
        <Pressable
          onPress={() => navigation.goBack()}
          accessibilityRole="button"
          accessibilityLabel="Voltar"
          hitSlop={8}
          style={estilos.voltar}
        >
          <Icone nome="voltar" cor={cores.texto} tamanho={22} espessura={2} />
        </Pressable>
        <TituloTela
          rotulo={carregando ? 'Carregando…' : `${fotos.length} ${fotos.length === 1 ? 'foto' : 'fotos'}`}
          fino="Catálogo de"
          forte="fotos"
        />
        <CampoBusca valor={busca} onChange={setBusca} placeholder="Time, seleção ou ano" rotuloAcessivel="Buscar fotos" />
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
        <MensagemErro mensagem={erro} />
      </View>

      <FlatList
        data={fotos}
        keyExtractor={(f) => f.arquivo}
        numColumns={COLUNAS}
        columnWrapperStyle={estilos.linha}
        contentContainerStyle={estilos.grade}
        keyboardShouldPersistTaps="handled"
        keyboardDismissMode="on-drag"
        initialNumToRender={18}
        windowSize={7}
        removeClippedSubviews
        renderItem={({ item }) => (
          <Pressable
            onPress={() => escolher(item)}
            accessibilityRole="button"
            accessibilityLabel={`Escolher foto: ${item.nome}`}
            style={({ pressed }) => [estilos.celula, pressed && estilos.pressionado]}
          >
            <Image source={{ uri: urlDaImagem(item.miniaturaUrl) }} style={estilos.miniatura} />
            <Text style={estilos.nome} numberOfLines={2}>
              {item.nome.replace(/^Camisa /, '')}
            </Text>
          </Pressable>
        )}
        ListEmptyComponent={
          carregando ? (
            <ActivityIndicator color={cores.destaque} style={estilos.carregando} />
          ) : erro ? null : (
            <EstadoVazio icone="imagem" titulo="Nenhuma foto encontrada" texto="Tente outro nome ou outra categoria." />
          )
        }
      />
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  cabecalho: { paddingHorizontal: espacos.lg, paddingBottom: espacos.sm, gap: espacos.md },
  voltar: { width: 44, height: 44, marginLeft: -10, marginTop: espacos.xs, alignItems: 'center', justifyContent: 'center' },
  faixaChips: { marginHorizontal: -espacos.lg },
  chips: { gap: espacos.sm, paddingHorizontal: espacos.lg },
  grade: { paddingHorizontal: espacos.lg - 4, paddingBottom: espacos.xl },
  linha: { gap: espacos.sm },
  celula: { flex: 1, maxWidth: `${100 / COLUNAS}%`, marginBottom: espacos.md - 4 },
  pressionado: { opacity: 0.7 },
  miniatura: { width: '100%', aspectRatio: 1, borderRadius: raios.md, backgroundColor: cores.superficieAlta },
  nome: { fontFamily: fontes.media, fontSize: 11, lineHeight: 14, color: cores.textoSecundario, marginTop: espacos.xs },
  carregando: { marginTop: espacos.xxl },
});
