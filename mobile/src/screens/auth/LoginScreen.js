import { useRef, useState } from 'react';
import { StyleSheet, View } from 'react-native';

import Botao from '../../components/Botao';
import CampoTexto from '../../components/CampoTexto';
import LayoutComRodape from '../../components/LayoutComRodape';
import LinhasDoCampo from '../../components/LinhasDoCampo';
import Logo from '../../components/Logo';
import MensagemErro from '../../components/MensagemErro';
import { useAuth } from '../../contexts/AuthContext';
import { mensagemDeErro } from '../../services/api';
import { espacos } from '../../theme';

/**
 * RF02 – Login. Depois de entrar, o RootNavigator troca sozinho para a
 * área do perfil do usuário (VENDEDOR ou COMPRADOR).
 */
export default function LoginScreen({ navigation }) {
  const { entrar } = useAuth();
  const campoSenha = useRef(null);

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function aoEntrar() {
    const novosErros = {};
    if (!email.trim()) novosErros.email = 'Informe o e-mail.';
    if (!senha) novosErros.senha = 'Informe a senha.';
    setErros(novosErros);
    setErroGeral('');
    if (Object.keys(novosErros).length > 0) return;

    setEnviando(true);
    try {
      await entrar(email.trim(), senha);
    } catch (erro) {
      setErroGeral(mensagemDeErro(erro));
      setEnviando(false); // só volta ao normal no erro; no sucesso a tela é trocada
    }
  }

  return (
    <LayoutComRodape
      rodape={
        <>
          <Botao titulo="Entrar em campo" onPress={aoEntrar} carregando={enviando} />
          <Botao
            titulo="Ainda não tem conta? Cadastre-se"
            variante="link"
            onPress={() => navigation.navigate('Cadastro')}
            desabilitado={enviando}
          />
        </>
      }
    >
      <LinhasDoCampo />
      <View style={estilos.topo}>
        <Logo />
      </View>

      <View style={estilos.espaco} />

      <MensagemErro mensagem={erroGeral} />

      <CampoTexto
        rotulo="E-mail"
        value={email}
        onChangeText={setEmail}
        erro={erros.email}
        placeholder="voce@email.com"
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
        textContentType="emailAddress"
        returnKeyType="next"
        onSubmitEditing={() => campoSenha.current?.focus()}
      />
      <CampoTexto
        ref={campoSenha}
        rotulo="Senha"
        value={senha}
        onChangeText={setSenha}
        erro={erros.senha}
        placeholder="Sua senha"
        secureTextEntry
        autoComplete="current-password"
        textContentType="password"
        returnKeyType="go"
        onSubmitEditing={aoEntrar}
        style={estilos.ultimoCampo}
      />
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  topo: { marginTop: espacos.xxl },
  espaco: { flex: 1, minHeight: espacos.xl },
  ultimoCampo: { marginBottom: espacos.sm },
});
