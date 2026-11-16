import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

import { cores, espacos, fontes, raios, tipografia } from '../theme';
import { TAMANHOS } from '../utils/tamanhos';
import Botao from './Botao';
import Icone from './Icone';
import TituloTela from './TituloTela';

/** Guia de tamanhos (tabela de medidas) aberto pelo detalhe da camisa. */
export default function GuiaTamanhos({ visivel, guia, tamanhoSelecionado, onFechar }) {
  return (
    <Modal visible={visivel} animationType="slide" presentationStyle="pageSheet" onRequestClose={onFechar}>
      <SafeAreaProvider>
        <SafeAreaView style={estilos.tela} edges={['top', 'bottom']}>
          <View style={estilos.topo}>
            <TituloTela rotulo={guia.titulo} fino="Guia de" forte="tamanhos" />
            <Pressable onPress={onFechar} accessibilityRole="button" accessibilityLabel="Fechar" hitSlop={10} style={estilos.fechar}>
              <Icone nome="mais" cor={cores.texto} tamanho={26} espessura={2} />
            </Pressable>
          </View>

          <ScrollView contentContainerStyle={estilos.conteudo}>
            <Text style={estilos.ajuda}>Medidas da camisa em centímetros, com a peça esticada sobre uma mesa.</Text>

            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
              <View style={estilos.tabela}>
                <View style={[estilos.linha, estilos.cabecalho]}>
                  <Text style={[estilos.celulaTamanho, estilos.textoCabecalho]}>Tam.</Text>
                  {guia.colunas.map((c) => (
                    <Text key={c} style={[estilos.celula, estilos.textoCabecalho]}>
                      {c}
                    </Text>
                  ))}
                </View>
                {TAMANHOS.map((t) => {
                  const ativo = t === tamanhoSelecionado;
                  return (
                    <View key={t} style={[estilos.linha, ativo && estilos.linhaAtiva]}>
                      <Text style={[estilos.celulaTamanho, estilos.tamanho, ativo && estilos.tamanhoAtivo]}>{t}</Text>
                      {guia.linhas[t].map((valor, i) => (
                        <Text key={guia.colunas[i]} style={estilos.celula}>
                          {valor}
                        </Text>
                      ))}
                    </View>
                  );
                })}
              </View>
            </ScrollView>

            <Text style={estilos.nota}>Considere uma margem de erro de 1 a 3 cm em cada medida.</Text>
            <Text style={estilos.nota}>
              Comprimento: da gola até a barra. Largura: de uma axila à outra. Na dúvida entre dois tamanhos, escolha o maior.
            </Text>
          </ScrollView>

          <View style={estilos.rodape}>
            <Botao titulo="Entendi" onPress={onFechar} />
          </View>
        </SafeAreaView>
      </SafeAreaProvider>
    </Modal>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  topo: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', padding: espacos.lg, paddingBottom: 0 },
  fechar: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center', transform: [{ rotate: '45deg' }] },
  conteudo: { padding: espacos.lg, gap: espacos.md },
  ajuda: { ...tipografia.corpo, fontSize: 15, color: cores.textoSecundario },
  tabela: { borderWidth: 1, borderColor: cores.borda, borderRadius: raios.md, overflow: 'hidden' },
  linha: { flexDirection: 'row', borderTopWidth: 1, borderTopColor: cores.borda },
  cabecalho: { borderTopWidth: 0, backgroundColor: cores.superficie },
  linhaAtiva: { backgroundColor: cores.superficieAlta },
  celulaTamanho: { width: 56, paddingVertical: espacos.sm + 4, paddingHorizontal: espacos.sm, textAlign: 'center' },
  celula: {
    width: 92,
    paddingVertical: espacos.sm + 4,
    paddingHorizontal: espacos.sm,
    textAlign: 'center',
    fontFamily: fontes.mono,
    fontSize: 13,
    color: cores.texto,
  },
  textoCabecalho: { ...tipografia.rotulo, fontSize: 9, letterSpacing: 1, color: cores.textoSecundario },
  tamanho: { fontFamily: fontes.pesada, fontSize: 15, color: cores.texto },
  tamanhoAtivo: { color: cores.destaque },
  nota: { ...tipografia.pequeno, color: cores.textoSecundario },
  rodape: { paddingHorizontal: espacos.lg, paddingTop: espacos.sm, paddingBottom: espacos.md },
});
