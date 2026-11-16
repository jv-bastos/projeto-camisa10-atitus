import { Alert, Image, Modal, StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { cores, espacos, fontes, raios, tipografia } from '../theme';
import { nomeDaCor } from '../utils/coresCamisa';
import { formatarPreco } from '../utils/formatar';
import { descreverGrade } from '../utils/tamanhos';
import Botao from './Botao';
import CamisaDesenho from './CamisaDesenho';
import FotoProduto from './FotoProduto';
import LayoutComRodape from './LayoutComRodape';
import TituloTela from './TituloTela';

const PRECO_BAIXO = 100;

function unidades(n) {
  return `${n} ${n === 1 ? 'unidade' : 'unidades'}`;
}

/**
 * Tela de revisão antes de cadastrar/salvar um produto: mostra tudo o que vai
 * ser gravado para o vendedor conferir (ex.: R$ 29,90 no lugar de R$ 299,90,
 * ou camisa branca marcada como preta). Na edição, marca o que mudou.
 *
 * dados:    { nome, descricao, preco, estoques: { P, M, G, GG, XGG }, categorias: [nomes], corPrincipal, corTexto,
 *             fotoUri, fotoCaminho, origemFoto: 'catalogo' | 'camera' | 'galeria' | null }
 * original: o produto antes da edição (ou null no cadastro)
 */
export default function RevisaoProduto({ visivel, dados, original, salvando, onConfirmar, onVoltar }) {
  if (!dados) return null;
  const ehEdicao = Boolean(original);
  const antes = original
    ? {
        nome: original.nome,
        categorias: original.categorias.map((c) => c.nome).join(' · '),
        preco: formatarPreco(original.preco),
        estoque: unidades(original.estoque),
        grade: descreverGrade(Object.fromEntries(original.tamanhos.map((t) => [t.tamanho, t.quantidade]))),
        descricao: original.descricao || '—',
        ano: original.ano ? String(original.ano) : '—',
        marca: original.marca || '—',
        corPrincipal: nomeDaCor(original.corPrincipal),
        corTexto: nomeDaCor(original.corTexto),
      }
    : {};
  const agora = {
    nome: dados.nome,
    categorias: dados.categorias.join(' · '),
    preco: formatarPreco(dados.preco),
    estoque: unidades(Object.values(dados.estoques).reduce((soma, q) => soma + q, 0)),
    grade: descreverGrade(dados.estoques),
    descricao: dados.descricao || '—',
    ano: String(dados.ano),
    marca: dados.marca || '—',
    corPrincipal: nomeDaCor(dados.corPrincipal),
    corTexto: nomeDaCor(dados.corTexto),
  };

  // foto nova vinda da câmera ou da galeria (fora do catálogo do projeto): pede confirmação
  const foraDoCatalogo = dados.origemFoto === 'camera' || dados.origemFoto === 'galeria';
  const origemTexto = dados.origemFoto === 'camera' ? 'da câmera' : 'da galeria';

  function confirmar() {
    if (!foraDoCatalogo) {
      onConfirmar();
      return;
    }
    Alert.alert(
      'Usar esta foto?',
      `Este produto vai ser ${ehEdicao ? 'salvo' : 'cadastrado'} com uma foto ${origemTexto}, que não faz parte do catálogo de fotos da loja. Confira se ela mostra a camisa certa e está nítida.`,
      [
        { text: 'Voltar e trocar', style: 'cancel', onPress: onVoltar },
        { text: 'Usar esta foto', onPress: onConfirmar },
      ],
    );
  }

  // props comuns de cada linha: valor atual, valor anterior e se mudou
  const linha = (campo) => ({ valor: agora[campo], antes: antes[campo], mudou: ehEdicao && antes[campo] !== agora[campo] });

  return (
    <Modal visible={visivel} animationType="slide" presentationStyle="fullScreen" onRequestClose={onVoltar} statusBarTranslucent>
      {/* o Modal abre fora da árvore principal, então precisa do próprio provedor de área segura */}
      <SafeAreaProvider>
        <LayoutComRodape
          rodape={
            <>
              <Botao
                titulo={ehEdicao ? 'Confirmar alterações' : 'Confirmar cadastro'}
                icone="check"
                onPress={confirmar}
                carregando={salvando}
              />
              <Botao titulo="Voltar e corrigir" variante="link" onPress={onVoltar} desabilitado={salvando} />
            </>
          }
        >
          <TituloTela rotulo="Revisão" fino="Confira o" forte={ehEdicao ? 'produto' : 'cadastro'} style={estilos.titulo} />
          <Text style={estilos.ajuda}>
            {ehEdicao
              ? 'Revise antes de salvar. O que mudou aparece marcado.'
              : 'Revise os dados antes de colocar a camisa na vitrine.'}
          </Text>

          <View style={estilos.imagens}>
            <View style={estilos.blocoImagem}>
              {dados.fotoUri ? (
                <Image source={{ uri: dados.fotoUri }} style={estilos.foto} accessibilityLabel="Foto do produto" />
              ) : (
                <FotoProduto caminho={dados.fotoCaminho} style={estilos.foto} tamanhoIcone={40} />
              )}
              <Text style={estilos.legenda}>Foto</Text>
            </View>
            <View style={estilos.blocoImagem}>
              <View style={[estilos.foto, estilos.fundoDesenho]}>
                <CamisaDesenho corPrincipal={dados.corPrincipal} corTexto={dados.corTexto} tamanho={110} />
              </View>
              <Text style={estilos.legenda}>Cores escolhidas</Text>
            </View>
          </View>

          {foraDoCatalogo ? (
            <View style={estilos.aviso} accessibilityRole="alert">
              <Text style={estilos.textoAviso}>
                Foto {origemTexto}, fora do catálogo da loja. Confira se ela mostra a camisa certa antes de confirmar.
              </Text>
            </View>
          ) : null}

          {dados.preco < PRECO_BAIXO ? (
            <View style={estilos.aviso} accessibilityRole="alert">
              <Text style={estilos.textoAviso}>
                Atenção: o preço está abaixo de {formatarPreco(PRECO_BAIXO)}. Confira se não faltou um dígito.
              </Text>
            </View>
          ) : null}

          <LinhaRevisao rotulo="Preço" destaque {...linha('preco')} />
          <LinhaRevisao rotulo="Nome" {...linha('nome')} />
          <LinhaRevisao rotulo="Categorias" {...linha('categorias')} />
          <LinhaRevisao rotulo="Ano" {...linha('ano')} />
          <LinhaRevisao rotulo="Marca" {...linha('marca')} />
          <LinhaRevisao rotulo="Estoque total" {...linha('estoque')} />
          <LinhaRevisao rotulo="Estoque por tamanho" {...linha('grade')} />
          <LinhaRevisao rotulo="Cor da camisa" cor={dados.corPrincipal} {...linha('corPrincipal')} />
          <LinhaRevisao rotulo="Cor do nome e número" cor={dados.corTexto} {...linha('corTexto')} />
          <LinhaRevisao rotulo="Descrição" {...linha('descricao')} />
        </LayoutComRodape>
      </SafeAreaProvider>
    </Modal>
  );
}

function LinhaRevisao({ rotulo, valor, antes, mudou, destaque, cor }) {
  return (
    <View style={estilos.linha}>
      <View style={estilos.linhaTopo}>
        <Text style={estilos.rotulo}>{rotulo}</Text>
        {mudou ? <Text style={estilos.alterado}>Alterado</Text> : null}
      </View>
      <View style={estilos.valorComCor}>
        {cor ? <View style={[estilos.amostra, { backgroundColor: cor }]} /> : null}
        <Text style={[estilos.valor, destaque && estilos.valorDestaque]}>{valor}</Text>
      </View>
      {mudou ? <Text style={estilos.antes}>Antes: {antes}</Text> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  titulo: { marginTop: espacos.md },
  ajuda: { ...tipografia.corpo, fontSize: 15, color: cores.textoSecundario, marginTop: espacos.sm, marginBottom: espacos.lg },
  imagens: { flexDirection: 'row', gap: espacos.md, marginBottom: espacos.lg },
  blocoImagem: { flex: 1, gap: espacos.xs },
  foto: { width: '100%', aspectRatio: 1, borderRadius: raios.md, borderWidth: 1, borderColor: cores.borda },
  fundoDesenho: { backgroundColor: cores.superficie, alignItems: 'center', justifyContent: 'center' },
  legenda: { ...tipografia.rotulo, fontSize: 10, color: cores.textoSecundario, textAlign: 'center' },
  aviso: {
    backgroundColor: 'rgba(245, 197, 24, 0.12)',
    borderWidth: 1,
    borderColor: cores.destaque,
    borderRadius: raios.sm,
    padding: espacos.md,
    marginBottom: espacos.md,
  },
  textoAviso: { ...tipografia.pequeno, fontSize: 14, color: cores.texto },
  linha: { paddingVertical: espacos.md - 2, borderBottomWidth: 1, borderBottomColor: cores.divisoria, gap: 4 },
  linhaTopo: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  alterado: { ...tipografia.rotulo, fontSize: 10, color: cores.destaque },
  valorComCor: { flexDirection: 'row', alignItems: 'center', gap: espacos.sm },
  amostra: { width: 22, height: 22, borderRadius: raios.redondo, borderWidth: 1, borderColor: cores.bordaForte },
  valor: { ...tipografia.corpo, color: cores.texto, flexShrink: 1 },
  valorDestaque: { fontFamily: fontes.monoMedia, fontSize: 26, color: cores.destaque },
  antes: { ...tipografia.pequeno, color: cores.textoSecundario, textDecorationLine: 'line-through' },
});
