import { useRef, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import Botao from '../../components/Botao';
import CampoTexto from '../../components/CampoTexto';
import Icone from '../../components/Icone';
import LayoutComRodape from '../../components/LayoutComRodape';
import LinhasDoCampo from '../../components/LinhasDoCampo';
import MensagemErro from '../../components/MensagemErro';
import { useAuth } from '../../contexts/AuthContext';
import { errosDosCampos, mensagemDeErro } from '../../services/api';
import { cores, espacos, raios, tipografia } from '../../theme';

const PERFIS = [
  { valor: 'COMPRADOR', titulo: 'Quero comprar', descricao: 'Escolha e compre suas camisas', icone: 'carrinho' },
  { valor: 'VENDEDOR', titulo: 'Quero vender', descricao: 'Cadastre camisas e acompanhe vendas', icone: 'camisa' },
];

const EMAIL_VALIDO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/**
 * RF01 – Cadastro com nome, e-mail, senha e perfil.
 * As mesmas regras são validadas de novo no backend; erros dele aparecem em cada campo.
 */
export default function CadastroScreen({ navigation }) {
  const { cadastrar } = useAuth();
  const campoEmail = useRef(null);
  const campoSenha = useRef(null);

  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [perfil, setPerfil] = useState(null);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [enviando, setEnviando] = useState(false);

  function validar() {
    const novosErros = {};
    if (!nome.trim()) novosErros.nome = 'Informe o nome.';
    if (!EMAIL_VALIDO.test(email.trim())) novosErros.email = 'Informe um e-mail válido.';
    if (senha.length < 6) novosErros.senha = 'A senha deve ter pelo menos 6 caracteres.';
    if (!perfil) novosErros.perfil = 'Escolha se você quer comprar ou vender.';
    return novosErros;
  }

  async function aoCadastrar() {
    const novosErros = validar();
    setErros(novosErros);
    setErroGeral('');
    if (Object.keys(novosErros).length > 0) return;

    setEnviando(true);
    try {
      await cadastrar({ nome: nome.trim(), email: email.trim(), senha, perfil });
    } catch (erro) {
      setErros(errosDosCampos(erro));
      setErroGeral(mensagemDeErro(erro)); // ex.: "Este e-mail já está cadastrado."
      setEnviando(false);
    }
  }

  return (
    <LayoutComRodape
      rodape={
        <>
          <Botao titulo="Criar conta" onPress={aoCadastrar} carregando={enviando} />
          <Botao titulo="Já tenho conta" variante="link" onPress={() => navigation.goBack()} desabilitado={enviando} />
        </>
      }
    >
      <LinhasDoCampo top={-200} left={60} />
      <Text style={estilos.rotuloTopo}>Nova conta</Text>
      <Text style={estilos.titulo}>
        Entre em <Text style={estilos.tituloForte}>campo</Text>
      </Text>

      <MensagemErro mensagem={erroGeral} />

      <Text style={estilos.rotulo}>Como você vai usar o app?</Text>
      <View style={estilos.perfis} accessibilityRole="radiogroup">
        {PERFIS.map((opcao) => {
          const selecionado = perfil === opcao.valor;
          return (
            <Pressable
              key={opcao.valor}
              onPress={() => setPerfil(opcao.valor)}
              accessibilityRole="radio"
              accessibilityState={{ checked: selecionado }}
              style={[estilos.cartaoPerfil, selecionado && estilos.cartaoSelecionado]}
            >
              <Icone nome={opcao.icone} cor={selecionado ? cores.destaque : cores.textoSecundario} tamanho={26} espessura={1.8} />
              <Text style={[estilos.tituloPerfil, selecionado && { color: cores.destaque }]}>{opcao.titulo}</Text>
              <Text style={estilos.descricaoPerfil}>{opcao.descricao}</Text>
            </Pressable>
          );
        })}
      </View>
      {erros.perfil ? <Text style={estilos.erro}>{erros.perfil}</Text> : null}

      <CampoTexto
        rotulo="Nome"
        value={nome}
        onChangeText={setNome}
        erro={erros.nome}
        placeholder="Seu nome ou o nome da loja"
        autoComplete="name"
        returnKeyType="next"
        onSubmitEditing={() => campoEmail.current?.focus()}
      />
      <CampoTexto
        ref={campoEmail}
        rotulo="E-mail"
        value={email}
        onChangeText={setEmail}
        erro={erros.email}
        placeholder="voce@email.com"
        keyboardType="email-address"
        autoCapitalize="none"
        autoComplete="email"
        returnKeyType="next"
        onSubmitEditing={() => campoSenha.current?.focus()}
      />
      <CampoTexto
        ref={campoSenha}
        rotulo="Senha"
        value={senha}
        onChangeText={setSenha}
        erro={erros.senha}
        placeholder="Mínimo de 6 caracteres"
        secureTextEntry
        autoComplete="new-password"
        textContentType="newPassword"
        returnKeyType="go"
        onSubmitEditing={aoCadastrar}
      />
    </LayoutComRodape>
  );
}

const estilos = StyleSheet.create({
  rotuloTopo: { ...tipografia.rotulo, color: cores.destaque, marginTop: espacos.lg },
  titulo: { ...tipografia.tituloFino, color: cores.texto, marginTop: espacos.xs, marginBottom: espacos.xl },
  tituloForte: { ...tipografia.tituloForte },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario, marginBottom: espacos.sm },
  perfis: { flexDirection: 'row', gap: espacos.sm, marginBottom: espacos.sm },
  cartaoPerfil: {
    flex: 1,
    backgroundColor: cores.superficie,
    borderWidth: 1,
    borderColor: cores.borda,
    borderRadius: raios.md,
    padding: espacos.md,
    gap: espacos.xs,
  },
  cartaoSelecionado: { borderColor: cores.destaque, backgroundColor: cores.superficieAlta },
  tituloPerfil: { ...tipografia.subtitulo, color: cores.texto, marginTop: espacos.sm },
  descricaoPerfil: { ...tipografia.pequeno, color: cores.textoSecundario },
  erro: { ...tipografia.pequeno, color: cores.erro, marginBottom: espacos.md },
});
