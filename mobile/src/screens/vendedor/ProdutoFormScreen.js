import * as ImagePicker from 'expo-image-picker';
import { useEffect, useRef, useState } from 'react';
import { Alert, Image, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import Botao from '../../components/Botao';
import CampoTexto from '../../components/CampoTexto';
import Chip from '../../components/Chip';
import FotoProduto from '../../components/FotoProduto';
import Icone from '../../components/Icone';
import LayoutComRodape from '../../components/LayoutComRodape';
import MensagemErro from '../../components/MensagemErro';
import RevisaoProduto from '../../components/RevisaoProduto';
import TituloTela from '../../components/TituloTela';
import { errosDosCampos, mensagemDeErro, urlDaImagem } from '../../services/api';
import { listarCategorias } from '../../services/catalogoService';
import { enviarFoto, salvarProduto, usarFotoDoCatalogo } from '../../services/vendedorService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { nomeDaCor, PALETA } from '../../utils/coresCamisa';
import { MARCAS } from '../../utils/marcas';
import { estoqueDoTamanho, TAMANHOS } from '../../utils/tamanhos';

const CORES_PALETA = PALETA.map((c) => c.hex);

const OPCOES_FOTO = { mediaTypes: ['images'], allowsEditing: true, aspect: [1, 1], quality: 0.7 };

/**
 * Lê o preço digitado: "299,90", "1.299,90" ou "299.90" → número.
 * Com vírgula, o ponto é separador de milhar; sem vírgula, o ponto é o decimal
 * (alguns teclados Android só mostram ponto). Texto inválido → NaN.
 */
function lerPreco(texto) {
  const limpo = String(texto).trim();
  if (!limpo) return NaN;
  const normalizado = limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo;
  return /^\d+(\.\d{1,2})?$/.test(normalizado) ? Number(normalizado) : NaN;
}

function mesmaLista(a, b) {
  return a.length === b.length && a.every((x) => b.includes(x));
}

/**
 * RF04/RF05/RF08 – cadastro e edição de camisa.
 * Sem "produto" nos parâmetros = novo; com "produto" = edição.
 * Foto: do catálogo do projeto, da galeria ou da câmera. Ela é aplicada depois de
 * salvar os dados (o backend precisa do id do produto).
 * Ao escolher uma foto do catálogo num produto NOVO, o nome e as categorias
 * sugeridos são preenchidos (só se o vendedor ainda não tiver mexido neles).
 */
export default function ProdutoFormScreen({ route, navigation }) {
  const original = route.params?.produto ?? null;
  const campoPreco = useRef(null);

  const [produtoId, setProdutoId] = useState(original?.id ?? null);
  const [nome, setNome] = useState(original?.nome ?? '');
  const [descricao, setDescricao] = useState(original?.descricao ?? '');
  const [preco, setPreco] = useState(original ? Number(original.preco).toFixed(2).replace('.', ',') : '');
  // estoque por tamanho, como texto dos campos: { P: '2', M: '5', ... } ('' = 0)
  const [estoques, setEstoques] = useState(() =>
    Object.fromEntries(TAMANHOS.map((t) => [t, original ? String(estoqueDoTamanho(original, t)) : ''])),
  );
  const [categoriaIds, setCategoriaIds] = useState(original?.categorias?.map((c) => c.id) ?? []);
  const [ano, setAno] = useState(original?.ano ? String(original.ano) : '');
  const [marca, setMarca] = useState(original?.marca ?? null);
  const [corPrincipal, setCorPrincipal] = useState(original?.corPrincipal ?? '#FFFFFF');
  const [corTexto, setCorTexto] = useState(original?.corTexto ?? '#111111');
  const [fotoNova, setFotoNova] = useState(null); // foto do aparelho (galeria/câmera)
  const [fotoCatalogo, setFotoCatalogo] = useState(null); // foto do catálogo do projeto
  const sugestao = useRef({ nome: '', categoriaIds: [], ano: '' }); // última sugestão aplicada pelo catálogo

  const [categorias, setCategorias] = useState([]);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [salvando, setSalvando] = useState(false);
  const [revisao, setRevisao] = useState(null); // dados mostrados na tela de revisão (null = fechada)

  useEffect(() => {
    listarCategorias()
      .then(setCategorias)
      .catch((e) => setErroGeral(mensagemDeErro(e)));
  }, []);

  // volta do catálogo com a foto escolhida (CatalogoFotosScreen → popTo com { fotoCatalogo })
  const fotoRecebida = route.params?.fotoCatalogo;
  useEffect(() => {
    if (!fotoRecebida) return;
    setFotoCatalogo(fotoRecebida);
    setFotoNova(null);
    if (!original) {
      const ids = fotoRecebida.categorias.map((c) => c.id);
      // só substitui o que ainda está vazio ou igual à sugestão anterior
      setNome((atual) => (!atual.trim() || atual === sugestao.current.nome ? fotoRecebida.nome : atual));
      if (fotoRecebida.ano) {
        setAno((atual) => (!atual || atual === sugestao.current.ano ? String(fotoRecebida.ano) : atual));
      }
      setCategoriaIds((atuais) =>
        atuais.length === 0 || mesmaLista(atuais, sugestao.current.categoriaIds) ? ids : atuais,
      );
      sugestao.current = { nome: fotoRecebida.nome, categoriaIds: ids, ano: fotoRecebida.ano ? String(fotoRecebida.ano) : '' };
    }
    navigation.setParams({ fotoCatalogo: undefined });
  }, [fotoRecebida, original, navigation]);

  function alternarCategoria(id) {
    setCategoriaIds((atuais) => (atuais.includes(id) ? atuais.filter((x) => x !== id) : [...atuais, id]));
  }

  async function escolherFoto(origem) {
    if (origem === 'camera') {
      const permissao = await ImagePicker.requestCameraPermissionsAsync();
      if (!permissao.granted) {
        Alert.alert('Sem acesso à câmera', 'Libere o acesso à câmera nas configurações do celular para tirar a foto.');
        return;
      }
    }
    const resultado =
      origem === 'camera'
        ? await ImagePicker.launchCameraAsync(OPCOES_FOTO)
        : await ImagePicker.launchImageLibraryAsync(OPCOES_FOTO);
    if (!resultado.canceled) {
      setFotoNova({ ...resultado.assets[0], origem }); // origem: 'camera' | 'galeria' (avisado na revisão)
      setFotoCatalogo(null);
    }
  }

  function validar() {
    const novosErros = {};
    const valorPreco = lerPreco(preco);
    const valoresEstoque = Object.fromEntries(TAMANHOS.map((t) => [t, Number(estoques[t] || 0)]));
    if (!nome.trim()) novosErros.nome = 'Informe o nome do produto.';
    if (!(valorPreco > 0)) novosErros.preco = 'Informe um preço válido, ex.: 299,90.';
    if (TAMANHOS.every((t) => estoques[t] === '')) novosErros.estoques = 'Informe o estoque de pelo menos um tamanho.';
    if (categoriaIds.length === 0) novosErros.categoriaIds = 'Escolha pelo menos uma categoria.';
    const valorAno = Number(ano);
    if (!ano || valorAno < 1900 || valorAno > 2100) novosErros.ano = 'Informe o ano da camisa, ex.: 2026 (temporada 26/27).';
    return { novosErros, valorPreco, valoresEstoque };
  }

  /** 1º passo: valida e abre a revisão com tudo o que vai ser gravado. */
  function aoRevisar() {
    const { novosErros, valorPreco, valoresEstoque } = validar();
    setErros(novosErros);
    setErroGeral('');
    if (Object.keys(novosErros).length > 0) return;

    setRevisao({
      nome: nome.trim(),
      descricao: descricao.trim(),
      preco: valorPreco,
      estoques: valoresEstoque,
      ano: Number(ano),
      marca,
      categoriaIds,
      categorias: categorias.filter((c) => categoriaIds.includes(c.id)).map((c) => c.nome),
      corPrincipal,
      corTexto,
      fotoUri: fotoNova ? fotoNova.uri : fotoCatalogo ? urlDaImagem(fotoCatalogo.fotoUrl) : null,
      fotoCaminho: original?.imagemUrl ?? null,
      // foto nova escolhida agora: do catálogo do projeto ou do aparelho (câmera/galeria)
      origemFoto: fotoNova ? fotoNova.origem : fotoCatalogo ? 'catalogo' : null,
    });
  }

  /** 2º passo (botão "Confirmar" da revisão): grava os dados e depois a foto. */
  async function aoConfirmar() {
    const { nome: nomeFinal, descricao: descricaoFinal, preco: precoFinal, estoques: estoquesFinais } = revisao;
    setSalvando(true);
    let salvo = null;
    try {
      salvo = await salvarProduto(produtoId, {
        nome: nomeFinal,
        descricao: descricaoFinal,
        preco: precoFinal,
        estoques: estoquesFinais,
        ano: revisao.ano,
        marca: revisao.marca,
        categoriaIds: revisao.categoriaIds,
        corPrincipal: revisao.corPrincipal,
        corTexto: revisao.corTexto,
      });
      setProdutoId(salvo.id); // se a foto falhar, o próximo "Salvar" edita em vez de duplicar
    } catch (e) {
      // volta ao formulário mostrando os erros em cada campo
      setRevisao(null);
      setErros(errosDosCampos(e));
      setErroGeral(mensagemDeErro(e));
      setSalvando(false);
      return;
    }

    if (fotoNova || fotoCatalogo) {
      try {
        if (fotoNova) {
          await enviarFoto(salvo.id, fotoNova);
        } else {
          await usarFotoDoCatalogo(salvo.id, fotoCatalogo.arquivo);
        }
      } catch (e) {
        setRevisao(null);
        setErroGeral(`Os dados foram salvos, mas a foto não foi enviada: ${mensagemDeErro(e)}`);
        setSalvando(false);
        return;
      }
    }

    setSalvando(false);
    setRevisao(null);
    navigation.goBack();
  }

  return (
    <LayoutComRodape
      rodape={
        <>
          <Botao titulo={original ? 'Revisar alterações' : 'Revisar cadastro'} onPress={aoRevisar} carregando={salvando} />
          <Botao titulo="Cancelar" variante="link" onPress={() => navigation.goBack()} desabilitado={salvando} />
        </>
      }
    >
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
        rotulo={original ? 'Editar' : 'Cadastrar'}
        fino={original ? 'Editar' : 'Novo'}
        forte="produto"
        style={estilos.titulo}
      />

      <MensagemErro mensagem={erroGeral} />

      <Text style={estilos.rotulo}>Foto</Text>
      <View style={estilos.linhaFoto}>
        {fotoNova || fotoCatalogo ? (
          <Image
            source={{ uri: fotoNova ? fotoNova.uri : urlDaImagem(fotoCatalogo.fotoUrl) }}
            style={estilos.foto}
            accessibilityLabel="Foto escolhida"
          />
        ) : (
          <FotoProduto caminho={original?.imagemUrl} style={estilos.foto} tamanhoIcone={44} />
        )}
        <View style={estilos.botoesFoto}>
          <Botao titulo="Catálogo" icone="camisa" variante="secundario" onPress={() => navigation.navigate('CatalogoFotos')} />
          <Botao titulo="Galeria" icone="imagem" variante="secundario" onPress={() => escolherFoto('galeria')} />
          <Botao titulo="Câmera" icone="camera" variante="secundario" onPress={() => escolherFoto('camera')} />
        </View>
      </View>

      <CampoTexto
        rotulo="Nome"
        value={nome}
        onChangeText={setNome}
        erro={erros.nome}
        placeholder="Ex: Camisa Internacional 26/27"
        maxLength={120}
        returnKeyType="next"
        onSubmitEditing={() => campoPreco.current?.focus()}
      />

      <CampoTexto
        ref={campoPreco}
        rotulo="Preço (R$)"
        value={preco}
        onChangeText={setPreco}
        erro={erros.preco}
        placeholder="Ex: R$299,90"
        keyboardType="decimal-pad"
      />

      <CampoTexto
        rotulo="Ano"
        value={ano}
        onChangeText={(t) => setAno(t.replace(/\D/g, ''))}
        erro={erros.ano}
        placeholder="Ex: 2026"
        keyboardType="number-pad"
        maxLength={4}
      />

      <GradeEstoque
        valores={estoques}
        onChange={(t, valor) => setEstoques((atuais) => ({ ...atuais, [t]: valor.replace(/\D/g, '') }))}
        erro={erros.estoques}
      />

      <CampoTexto
        rotulo="Descrição"
        value={descricao}
        onChangeText={setDescricao}
        erro={erros.descricao}
        placeholder="Modelo, temporada, detalhes…"
        multiline
        maxLength={1000}
        textAlignVertical="top"
        style={estilos.descricao}
      />

      <Text style={estilos.rotulo}>Categorias · toque para marcar uma ou mais</Text>
      <View style={estilos.chips}>
        {categorias.map((c) => (
          <Chip key={c.id} rotulo={c.nome} ativo={categoriaIds.includes(c.id)} onPress={() => alternarCategoria(c.id)} />
        ))}
      </View>
      {erros.categoriaIds ? <Text style={estilos.erro}>{erros.categoriaIds}</Text> : null}

      <Text style={[estilos.rotulo, estilos.espacoAcima]}>Marca · opcional</Text>
      <View style={estilos.chips}>
        {MARCAS.map((m) => (
          <Chip key={m} rotulo={m} ativo={marca === m} onPress={() => setMarca(marca === m ? null : m)} />
        ))}
      </View>
      {erros.marca ? <Text style={estilos.erro}>{erros.marca}</Text> : null}

      <Text style={[estilos.rotulo, estilos.espacoAcima]}>Cor da camisa</Text>
      <SeletorCor valor={corPrincipal} onChange={setCorPrincipal} />
      {erros.corPrincipal ? <Text style={estilos.erro}>{erros.corPrincipal}</Text> : null}

      <Text style={[estilos.rotulo, estilos.espacoAcima]}>Cor do nome e número</Text>
      <SeletorCor valor={corTexto} onChange={setCorTexto} />
      {erros.corTexto ? <Text style={estilos.erro}>{erros.corTexto}</Text> : null}
      <Text style={estilos.ajuda}>As cores são usadas na prévia da personalização da camisa.</Text>

      <RevisaoProduto
        visivel={Boolean(revisao)}
        dados={revisao}
        original={original}
        salvando={salvando}
        onConfirmar={aoConfirmar}
        onVoltar={() => setRevisao(null)}
      />
    </LayoutComRodape>
  );
}

/** Estoque por tamanho: um campo pequeno para cada tamanho e o total somado. */
function GradeEstoque({ valores, onChange, erro }) {
  const total = TAMANHOS.reduce((soma, t) => soma + Number(valores[t] || 0), 0);
  return (
    <View style={estilos.grade}>
      <View style={estilos.linhaRotuloGrade}>
        <Text style={estilos.rotulo}>Estoque por tamanho</Text>
        <Text style={estilos.rotulo}>Total {total}</Text>
      </View>
      <View style={estilos.camposGrade}>
        {TAMANHOS.map((t) => (
          <View key={t} style={estilos.campoGrade}>
            <Text style={estilos.tamanhoGrade}>{t}</Text>
            <TextInput
              value={valores[t]}
              onChangeText={(v) => onChange(t, v)}
              placeholder="0"
              placeholderTextColor={cores.textoSecundario}
              keyboardType="number-pad"
              maxLength={4}
              selectionColor={cores.destaque}
              style={estilos.inputGrade}
              accessibilityLabel={`Estoque do tamanho ${t}`}
            />
          </View>
        ))}
      </View>
      {erro ? <Text style={estilos.erro}>{erro}</Text> : null}
      <Text style={estilos.ajuda}>Deixe em branco ou 0 os tamanhos que você não tem.</Text>
    </View>
  );
}

function SeletorCor({ valor, onChange }) {
  // se o produto tiver uma cor fora da paleta, ela aparece primeiro para não se perder
  const opcoes = CORES_PALETA.includes(valor) ? CORES_PALETA : [valor, ...CORES_PALETA];
  return (
    <View style={estilos.paleta} accessibilityRole="radiogroup">
      {opcoes.map((cor) => {
        const selecionada = cor === valor;
        return (
          <Pressable
            key={cor}
            onPress={() => onChange(cor)}
            accessibilityRole="radio"
            accessibilityState={{ checked: selecionada }}
            accessibilityLabel={`Cor ${nomeDaCor(cor)}`}
            style={[estilos.anel, selecionada && estilos.anelSelecionado]}
          >
            <View style={[estilos.amostra, { backgroundColor: cor }]} />
          </Pressable>
        );
      })}
    </View>
  );
}

const estilos = StyleSheet.create({
  flex: { flex: 1 },
  voltar: { width: 44, height: 44, marginLeft: -10, alignItems: 'center', justifyContent: 'center' },
  titulo: { marginTop: espacos.sm, marginBottom: espacos.lg },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario, marginBottom: espacos.sm },
  espacoAcima: { marginTop: espacos.lg },
  linhaFoto: { flexDirection: 'row', alignItems: 'flex-start', gap: espacos.md, marginBottom: espacos.lg },
  foto: { width: 136, height: 136, borderRadius: raios.md, borderWidth: 1, borderColor: cores.borda },
  botoesFoto: { flex: 1, gap: espacos.sm },
  grade: { marginBottom: espacos.lg },
  linhaRotuloGrade: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: espacos.sm },
  camposGrade: { flexDirection: 'row', gap: espacos.sm },
  campoGrade: { flex: 1, alignItems: 'center', gap: espacos.xs },
  tamanhoGrade: { fontFamily: fontes.pesada, fontSize: 14, color: cores.texto },
  inputGrade: {
    width: '100%',
    height: 48,
    borderWidth: 1,
    borderColor: cores.bordaForte,
    borderRadius: raios.sm,
    textAlign: 'center',
    fontFamily: fontes.monoMedia,
    fontSize: 16,
    color: cores.texto,
    paddingHorizontal: 2,
  },
  descricao: { marginBottom: espacos.lg },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: espacos.sm },
  erro: { ...tipografia.pequeno, color: cores.erro, marginTop: espacos.xs },
  paleta: { flexDirection: 'row', flexWrap: 'wrap', gap: espacos.sm },
  anel: {
    width: 44,
    height: 44,
    borderRadius: raios.redondo,
    borderWidth: 2,
    borderColor: 'transparent',
    alignItems: 'center',
    justifyContent: 'center',
  },
  anelSelecionado: { borderColor: cores.destaque },
  amostra: { width: 32, height: 32, borderRadius: raios.redondo, borderWidth: 1, borderColor: cores.bordaForte },
  ajuda: { ...tipografia.pequeno, color: cores.textoSecundario, marginTop: espacos.md },
});
