import { StyleSheet, Text } from 'react-native';

import Botao from '../components/Botao';
import LayoutComRodape from '../components/LayoutComRodape';
import { useAuth } from '../contexts/AuthContext';
import { cores, espacos, tipografia } from '../theme';

/**
 * TELA PROVISÓRIA: ocupa as abas que ainda serão implementadas
 * (área do vendedor e do comprador). Será removida depois.
 * Mostra o botão "Sair" para permitir testar o logout de qualquer perfil.
 */
export default function EmConstrucaoScreen({ route }) {
  const { usuario, sair } = useAuth();

  return (
    <LayoutComRodape rodape={<Botao titulo="Sair" variante="secundario" icone="sair" onPress={sair} />}>
      <Text style={estilos.rotuloTopo}>Em construção</Text>
      <Text style={estilos.titulo}>{route.name}</Text>
      <Text style={estilos.texto}>
        Olá, {usuario.nome}! Esta tela será construída na {route.params?.fase ?? 'próxima fase'}.
      </Text>
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  rotuloTopo: { ...tipografia.rotulo, color: cores.destaque, marginTop: espacos.md },
  titulo: { ...tipografia.tituloForte, color: cores.texto, marginTop: espacos.xs, marginBottom: espacos.md },
  texto: { ...tipografia.corpo, color: cores.textoSecundario },
});
