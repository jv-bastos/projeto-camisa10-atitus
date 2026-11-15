import { KeyboardAvoidingView, Platform, ScrollView, StyleSheet, View } from 'react-native';
import { SafeAreaView, useSafeAreaInsets } from 'react-native-safe-area-context';

import { cores, espacos } from '../theme';

const EH_IOS = Platform.OS === 'ios';

/**
 * Layout das telas com ação principal: o conteúdo rola e os botões ficam
 * FIXOS na parte de baixo, na zona do polegar.
 *
 * Teclado: só o CONTEÚDO se ajusta para o campo digitado continuar visível;
 * o rodapé NÃO sobe junto (fica atrás do teclado e volta quando ele fecha).
 * - iOS: a própria ScrollView desconta a altura do teclado e rola até o campo
 *   (automaticallyAdjustKeyboardInsets).
 * - Android: o KeyboardAvoidingView envolve só a ScrollView, então diminui
 *   a área rolável sem empurrar o rodapé.
 */
export default function LayoutComRodape({ children, rodape, comCabecalho = false }) {
  const { bottom } = useSafeAreaInsets();

  const conteudo = (
    <ScrollView
      style={estilos.flex}
      contentContainerStyle={estilos.conteudo}
      keyboardShouldPersistTaps="handled"
      keyboardDismissMode={EH_IOS ? 'interactive' : 'on-drag'}
      automaticallyAdjustKeyboardInsets={EH_IOS}
      showsVerticalScrollIndicator={false}
    >
      {children}
    </ScrollView>
  );

  return (
    // sem cabeçalho de navegação, a tela precisa respeitar a área do relógio/notch no topo
    <SafeAreaView style={estilos.tela} edges={comCabecalho ? [] : ['top']}>
      {EH_IOS ? (
        conteudo
      ) : (
        <KeyboardAvoidingView behavior="padding" style={estilos.flex}>
          {conteudo}
        </KeyboardAvoidingView>
      )}
      {rodape ? (
        <View style={[estilos.rodape, { paddingBottom: Math.max(bottom, espacos.md) }]}>{rodape}</View>
      ) : null}
    </SafeAreaView>
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  flex: { flex: 1 },
  conteudo: { flexGrow: 1, padding: espacos.lg },
  rodape: {
    paddingHorizontal: espacos.lg,
    paddingTop: espacos.md,
    gap: espacos.xs,
    backgroundColor: cores.barra,
    borderTopWidth: 1,
    borderTopColor: cores.divisoria,
  },
});
