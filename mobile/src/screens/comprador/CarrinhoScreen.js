import { useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import Botao from '../../components/Botao';
import EstadoVazio from '../../components/EstadoVazio';
import FotoProduto from '../../components/FotoProduto';
import Icone from '../../components/Icone';
import LayoutComRodape from '../../components/LayoutComRodape';
import MensagemErro from '../../components/MensagemErro';
import SeletorQuantidade from '../../components/SeletorQuantidade';
import TituloTela from '../../components/TituloTela';
import { useCarrinho } from '../../contexts/CarrinhoContext';
import { mensagemDeErro } from '../../services/api';
import { criarPedido } from '../../services/pedidoService';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';
import { formatarPreco } from '../../utils/formatar';
import { estoqueDoTamanho } from '../../utils/tamanhos';

/**
 * RF12/RF13 – carrinho: ajustar quantidades, remover itens e finalizar o pedido.
 * O total mostrado é uma prévia; o valor oficial é calculado pelo backend.
 */
export default function CarrinhoScreen({ navigation }) {
  const { itens, totalItens, total, alterarQuantidade, remover, limpar } = useCarrinho();
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState('');

  function confirmarFinalizacao() {
    Alert.alert('Finalizar pedido?', `${totalItens} ${totalItens === 1 ? 'camisa' : 'camisas'} · ${formatarPreco(total)}`, [
      { text: 'Voltar', style: 'cancel' },
      { text: 'Finalizar', onPress: finalizar },
    ]);
  }

  async function finalizar() {
    setErro('');
    setEnviando(true);
    try {
      const pedido = await criarPedido(
        itens.map((i) => ({ produtoId: i.produto.id, tamanho: i.tamanho, quantidade: i.quantidade })),
      );
      limpar();
      Alert.alert('Pedido realizado!', `Pedido nº ${pedido.id} · ${formatarPreco(pedido.valorTotal)}`, [
        { text: 'Ver meus pedidos', onPress: () => navigation.navigate('Pedidos') },
      ]);
    } catch (e) {
      setErro(mensagemDeErro(e)); // ex.: Estoque insuficiente para "Camisa X". Disponível: 2.
    } finally {
      setEnviando(false);
    }
  }

  if (itens.length === 0) {
    return (
      <LayoutComRodape>
        <TituloTela rotulo="Carrinho" fino="Seu" forte="carrinho" style={estilos.titulo} />
        <EstadoVazio
          icone="carrinho"
          titulo="Seu carrinho está vazio"
          texto="Escolha uma camisa na vitrine e ela aparece aqui."
          acao="Ver a vitrine"
          onAcao={() => navigation.navigate('Inicio')}
        />
      </LayoutComRodape>
    );
  }

  return (
    <LayoutComRodape
      rodape={
        <>
          <View style={estilos.resumo}>
            <Text style={estilos.rotulo}>
              {totalItens} {totalItens === 1 ? 'item' : 'itens'}
            </Text>
            <Text style={estilos.total}>{formatarPreco(total)}</Text>
          </View>
          <Botao titulo="Finalizar pedido" onPress={confirmarFinalizacao} carregando={enviando} />
        </>
      }
    >
      <TituloTela rotulo="Carrinho" fino="Seu" forte="carrinho" style={estilos.titulo} />
      <MensagemErro mensagem={erro} />

      {itens.map(({ chave, produto, tamanho, quantidade }) => (
        <View key={chave} style={estilos.item}>
          <FotoProduto caminho={produto.imagemUrl} style={estilos.foto} tamanhoIcone={28} />
          <View style={estilos.info}>
            <View style={estilos.linhaTopo}>
              <Text style={estilos.nome} numberOfLines={2}>
                {produto.nome}
              </Text>
              <Pressable
                onPress={() => remover(chave)}
                accessibilityRole="button"
                accessibilityLabel={`Remover ${produto.nome}, tamanho ${tamanho}`}
                hitSlop={10}
                style={estilos.lixeira}
              >
                <Icone nome="lixeira" cor={cores.textoSecundario} tamanho={20} espessura={1.8} />
              </Pressable>
            </View>
            <Text style={estilos.unitario}>
              Tamanho <Text style={estilos.tamanho}>{tamanho}</Text> · {formatarPreco(produto.preco)} cada
            </Text>
            <View style={estilos.linhaBaixo}>
              <SeletorQuantidade
                compacto
                valor={quantidade}
                onChange={(q) => alterarQuantidade(chave, q)}
                maximo={estoqueDoTamanho(produto, tamanho)}
              />
              <Text style={estilos.subtotal}>{formatarPreco(Number(produto.preco) * quantidade)}</Text>
            </View>
          </View>
        </View>
      ))}
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  titulo: { marginTop: espacos.sm, marginBottom: espacos.lg },
  item: {
    flexDirection: 'row',
    gap: espacos.md - 2,
    paddingVertical: espacos.md,
    borderBottomWidth: 1,
    borderBottomColor: cores.divisoria,
  },
  foto: { width: 84, height: 84, borderRadius: raios.md },
  info: { flex: 1, gap: espacos.xs },
  linhaTopo: { flexDirection: 'row', alignItems: 'flex-start', gap: espacos.sm },
  nome: { flex: 1, fontFamily: fontes.negrito, fontSize: 15, lineHeight: 20, color: cores.texto },
  lixeira: { padding: 2 },
  unitario: { ...tipografia.pequeno, color: cores.textoSecundario },
  tamanho: { fontFamily: fontes.pesada, color: cores.texto },
  linhaBaixo: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: espacos.xs },
  subtotal: { ...tipografia.preco, color: cores.destaque },
  resumo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: espacos.sm },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  total: { ...tipografia.preco, fontSize: 20, color: cores.destaque },
});
