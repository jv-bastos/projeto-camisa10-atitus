import { useEffect, useState } from 'react';
import { ActivityIndicator, Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import Botao from '../../components/Botao';
import FotoProduto from '../../components/FotoProduto';
import GuiaTamanhos from '../../components/GuiaTamanhos';
import Icone from '../../components/Icone';
import LayoutComRodape from '../../components/LayoutComRodape';
import MensagemErro from '../../components/MensagemErro';
import SeletorQuantidade from '../../components/SeletorQuantidade';
import { useCarrinho } from '../../contexts/CarrinhoContext';
import { mensagemDeErro } from '../../services/api';
import { buscarProduto } from '../../services/catalogoService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarPreco } from '../../utils/formatar';
import { estoqueDoTamanho, guiaDoProduto, TAMANHOS } from '../../utils/tamanhos';

/**
 * RF11 – detalhe da camisa: escolha do TAMANHO e da quantidade (RF12: adicionar ao carrinho).
 * Mostra na hora os dados que vieram da vitrine e atualiza com o backend
 * (preço e estoque podem ter mudado).
 */
export default function ProdutoScreen({ route, navigation }) {
  const { produtoId, produto: produtoDaVitrine } = route.params;
  const { adicionar, quantidadeNoCarrinho } = useCarrinho();

  const [produto, setProduto] = useState(produtoDaVitrine ?? null);
  const [tamanho, setTamanho] = useState(null);
  const [quantidade, setQuantidade] = useState(1);
  const [guiaAberto, setGuiaAberto] = useState(false);
  const [erro, setErro] = useState('');

  useEffect(() => {
    buscarProduto(produtoId)
      .then(setProduto)
      .catch((e) => setErro(mensagemDeErro(e)));
  }, [produtoId]);

  if (!produto) {
    return (
      <View style={estilos.centro}>
        {erro ? <MensagemErro mensagem={erro} /> : <ActivityIndicator color={cores.destaque} />}
      </View>
    );
  }

  // quanto ainda dá para adicionar de cada tamanho (estoque menos o que já está no carrinho)
  const livre = (t) => Math.max(estoqueDoTamanho(produto, t) - quantidadeNoCarrinho(produto.id, t), 0);
  const esgotado = produto.estoque === 0;
  const disponivel = tamanho ? livre(tamanho) : 0;
  const qtd = Math.min(quantidade, Math.max(disponivel, 1));
  const noCarrinho = tamanho ? quantidadeNoCarrinho(produto.id, tamanho) : 0;

  function escolherTamanho(t) {
    setTamanho(t);
    setQuantidade(1);
  }

  function aoAdicionar() {
    adicionar(produto, tamanho, qtd);
    setQuantidade(1);
    Alert.alert('Adicionado ao carrinho', `${qtd}x ${produto.nome} · tamanho ${tamanho}`, [
      { text: 'Continuar comprando', style: 'cancel', onPress: () => navigation.goBack() },
      { text: 'Ver carrinho', onPress: () => navigation.navigate('AreaComprador', { screen: 'Carrinho' }) },
    ]);
  }

  let tituloBotao = 'Adicionar';
  if (esgotado) tituloBotao = 'Esgotado';
  else if (!tamanho) tituloBotao = 'Escolha o tamanho';
  else if (disponivel === 0) tituloBotao = 'Tudo no carrinho';

  return (
    <LayoutComRodape
      rodape={
        <View style={estilos.barraCompra}>
          <View>
            <Text style={estilos.rotuloTotal}>Total</Text>
            <Text style={estilos.total}>{formatarPreco(Number(produto.preco) * (tamanho && disponivel ? qtd : 0))}</Text>
          </View>
          <Botao
            titulo={tituloBotao}
            icone={tamanho && disponivel ? 'carrinho' : undefined}
            onPress={aoAdicionar}
            desabilitado={!tamanho || disponivel === 0}
            style={estilos.botaoAdicionar}
          />
        </View>
      }
    >
      <View style={estilos.fotoArea}>
        <FotoProduto caminho={produto.imagemUrl} style={estilos.foto} tamanhoIcone={96} />
        <Pressable
          onPress={() => navigation.goBack()}
          accessibilityRole="button"
          accessibilityLabel="Voltar"
          style={estilos.voltar}
          hitSlop={8}
        >
          <Icone nome="voltar" cor={cores.texto} tamanho={22} espessura={2} />
        </Pressable>
      </View>

      <MensagemErro mensagem={erro} />

      <Text style={estilos.categoria}>{produto.categorias?.map((c) => c.nome).join(' · ')}</Text>
      <Text style={estilos.nome} accessibilityRole="header">
        {produto.nome}
      </Text>
      {produto.marca || produto.ano ? (
        <Text style={estilos.detalhes}>{[produto.marca, produto.ano].filter(Boolean).join(' · ')}</Text>
      ) : null}
      <Text style={estilos.preco}>{formatarPreco(produto.preco)}</Text>
      {esgotado ? <Text style={estilos.esgotado}>Esgotado em todos os tamanhos</Text> : null}

      {produto.descricao ? <Text style={estilos.descricao}>{produto.descricao}</Text> : null}

      <View style={estilos.secao}>
        <View style={estilos.linhaRotulo}>
          <Text style={estilos.rotulo}>Tamanho</Text>
          <Pressable onPress={() => setGuiaAberto(true)} accessibilityRole="button" hitSlop={10}>
            <Text style={estilos.link}>Guia de tamanhos</Text>
          </Pressable>
        </View>
        <View style={estilos.tamanhos} accessibilityRole="radiogroup">
          {TAMANHOS.map((t) => {
            const semEstoque = estoqueDoTamanho(produto, t) === 0;
            const ativo = t === tamanho;
            return (
              <Pressable
                key={t}
                onPress={() => escolherTamanho(t)}
                disabled={semEstoque}
                accessibilityRole="radio"
                accessibilityState={{ checked: ativo, disabled: semEstoque }}
                accessibilityLabel={`Tamanho ${t}${semEstoque ? ', esgotado' : ''}`}
                style={[estilos.botaoTamanho, ativo && estilos.botaoTamanhoAtivo, semEstoque && estilos.botaoTamanhoEsgotado]}
              >
                <Text
                  style={[estilos.textoTamanho, ativo && estilos.textoTamanhoAtivo, semEstoque && estilos.textoTamanhoEsgotado]}
                >
                  {t}
                </Text>
              </Pressable>
            );
          })}
        </View>
        {tamanho ? (
          <Text style={[estilos.estoque, estoqueDoTamanho(produto, tamanho) <= 3 && estilos.estoqueBaixo]}>
            {estoqueDoTamanho(produto, tamanho) <= 3
              ? `Últimas ${estoqueDoTamanho(produto, tamanho)} no tamanho ${tamanho}`
              : `${estoqueDoTamanho(produto, tamanho)} disponíveis no tamanho ${tamanho}`}
            {noCarrinho ? ` · ${noCarrinho} no seu carrinho` : ''}
          </Text>
        ) : null}
      </View>

      {tamanho && disponivel > 0 ? (
        <View style={estilos.linhaQuantidade}>
          <Text style={estilos.rotulo}>Quantidade</Text>
          <SeletorQuantidade valor={qtd} onChange={setQuantidade} maximo={disponivel} />
        </View>
      ) : null}

      <GuiaTamanhos
        visivel={guiaAberto}
        guia={guiaDoProduto(produto)}
        tamanhoSelecionado={tamanho}
        onFechar={() => setGuiaAberto(false)}
      />
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  centro: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: espacos.lg, backgroundColor: cores.fundo },
  fotoArea: { marginHorizontal: -espacos.sm, marginBottom: espacos.lg },
  foto: { width: '100%', aspectRatio: 1, borderRadius: raios.md, borderWidth: 1, borderColor: cores.borda },
  voltar: {
    position: 'absolute',
    top: espacos.sm,
    left: espacos.sm,
    width: 44,
    height: 44,
    borderRadius: raios.redondo,
    backgroundColor: 'rgba(8, 26, 16, 0.8)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  categoria: { ...tipografia.rotulo, color: cores.textoSecundario },
  nome: { fontFamily: fontes.negrito, fontSize: 24, lineHeight: 30, letterSpacing: -0.5, color: cores.texto, marginTop: espacos.xs },
  detalhes: { ...tipografia.pequeno, fontSize: 14, color: cores.textoSecundario, marginTop: espacos.xs },
  preco: { ...tipografia.preco, fontSize: 22, color: cores.destaque, marginTop: espacos.sm },
  esgotado: { ...tipografia.pequeno, fontFamily: fontes.negrito, color: cores.erro, marginTop: espacos.sm },
  descricao: { ...tipografia.corpo, fontSize: 15, lineHeight: 22, color: cores.texto, marginTop: espacos.lg },
  secao: {
    marginTop: espacos.lg,
    paddingTop: espacos.lg,
    borderTopWidth: 1,
    borderTopColor: cores.divisoria,
    gap: espacos.sm + 4,
  },
  linhaRotulo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  link: { ...tipografia.rotulo, color: cores.destaque, textDecorationLine: 'underline' },
  tamanhos: { flexDirection: 'row', gap: espacos.sm },
  botaoTamanho: {
    flex: 1,
    height: 48,
    borderRadius: raios.sm,
    borderWidth: 1,
    borderColor: cores.bordaForte,
    alignItems: 'center',
    justifyContent: 'center',
  },
  botaoTamanhoAtivo: { backgroundColor: cores.destaque, borderColor: cores.destaque },
  botaoTamanhoEsgotado: { borderColor: cores.borda, backgroundColor: 'transparent' },
  textoTamanho: { fontFamily: fontes.pesada, fontSize: 15, color: cores.texto },
  textoTamanhoAtivo: { color: cores.textoSobreDestaque },
  textoTamanhoEsgotado: { color: cores.bordaForte, textDecorationLine: 'line-through' },
  estoque: { ...tipografia.pequeno, color: cores.textoSecundario },
  estoqueBaixo: { color: cores.destaque },
  linhaQuantidade: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: espacos.lg,
    paddingTop: espacos.lg,
    borderTopWidth: 1,
    borderTopColor: cores.divisoria,
  },
  barraCompra: { flexDirection: 'row', alignItems: 'center', gap: espacos.md },
  rotuloTotal: { ...tipografia.rotulo, color: cores.textoSecundario },
  total: { ...tipografia.preco, fontSize: 20, color: cores.destaque },
  botaoAdicionar: { flex: 1 },
});
