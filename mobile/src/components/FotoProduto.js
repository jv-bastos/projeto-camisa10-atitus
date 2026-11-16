import { useState } from 'react';
import { Image, StyleSheet, View } from 'react-native';

import { urlDaImagem } from '../services/api';
import { cores } from '../theme';
import Icone from './Icone';

/**
 * Foto da camisa vinda do backend ("/uploads/..."). Sem foto, ou se ela não
 * carregar, mostra o desenho de uma camisa no lugar.
 * O tamanho vem do "style" de quem usa (ex.: { width: 72, height: 72 }).
 */
export default function FotoProduto({ caminho, style, tamanhoIcone = 40 }) {
  const [falhou, setFalhou] = useState(false);
  const uri = urlDaImagem(caminho);

  return (
    <View style={[estilos.moldura, style]}>
      {uri && !falhou ? (
        <Image
          source={{ uri }}
          style={StyleSheet.absoluteFill}
          resizeMode="cover"
          onError={() => setFalhou(true)}
          accessibilityIgnoresInvertColors
        />
      ) : (
        <Icone nome="camisa" cor={cores.bordaForte} tamanho={tamanhoIcone} espessura={1.5} />
      )}
    </View>
  );
}

const estilos = StyleSheet.create({
  moldura: {
    backgroundColor: cores.superficieAlta,
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
});
