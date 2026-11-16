import { StyleSheet, Text, View } from 'react-native';

import Botao from '../../components/Botao';
import LayoutComRodape from '../../components/LayoutComRodape';
import { useAuth } from '../../contexts/AuthContext';
import { cores, espacos, fontes, raios, tipografia } from '../../theme';

/** Perfil do usuário, com o logout (RF03) na zona do polegar. */
export default function PerfilScreen() {
  const { usuario, sair } = useAuth();
  const inicial = usuario.nome.trim().charAt(0).toUpperCase();

  return (
    <LayoutComRodape rodape={<Botao titulo="Sair da conta" variante="perigo" icone="sair" onPress={sair} />}>
      <Text style={estilos.rotuloTopo}>Minha conta</Text>
      <Text style={estilos.titulo}>
        Meu <Text style={estilos.tituloForte}>perfil</Text>
      </Text>

      <View style={estilos.cartao}>
        <View style={estilos.avatar}>
          <Text style={estilos.inicial}>{inicial}</Text>
        </View>
        <View style={estilos.dados}>
          <Text style={estilos.nome} numberOfLines={1}>
            {usuario.nome}
          </Text>
          <Text style={estilos.email} numberOfLines={1}>
            {usuario.email}
          </Text>
        </View>
      </View>

      <View style={estilos.linha}>
        <Text style={estilos.rotulo}>Perfil</Text>
        <Text style={estilos.valor}>{usuario.perfil === 'VENDEDOR' ? 'Vendedor' : 'Comprador'}</Text>
      </View>
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  rotuloTopo: { ...tipografia.rotulo, color: cores.textoSecundario, marginTop: espacos.md },
  titulo: { ...tipografia.tituloFino, color: cores.texto, marginTop: espacos.xs, marginBottom: espacos.lg },
  tituloForte: { ...tipografia.tituloForte },
  cartao: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: espacos.md,
    backgroundColor: cores.superficie,
    borderRadius: raios.md,
    padding: espacos.md,
  },
  avatar: {
    width: 64,
    height: 64,
    borderRadius: raios.redondo,
    backgroundColor: cores.destaque,
    alignItems: 'center',
    justifyContent: 'center',
  },
  inicial: { fontFamily: fontes.pesada, fontSize: 30, color: cores.textoSobreDestaque },
  dados: { flex: 1, gap: 2 },
  nome: { ...tipografia.subtitulo, color: cores.texto },
  email: { ...tipografia.pequeno, fontSize: 14, color: cores.textoSecundario },
  linha: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: espacos.md,
    borderBottomWidth: 1,
    borderBottomColor: cores.divisoria,
    marginTop: espacos.md,
  },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  valor: { ...tipografia.corpo, color: cores.texto },
});
