import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { ActivityIndicator, StyleSheet, View } from 'react-native';

import { useAuth } from '../contexts/AuthContext';
import CadastroScreen from '../screens/auth/CadastroScreen';
import LoginScreen from '../screens/auth/LoginScreen';
import { cores } from '../theme';
import CompradorTabs from './CompradorTabs';
import VendedorTabs from './VendedorTabs';

const Pilha = createNativeStackNavigator();

/**
 * Decide o que mostrar conforme o login (RF02):
 * - sem usuário → Login / Cadastro
 * - VENDEDOR    → abas do vendedor
 * - COMPRADOR   → abas do comprador
 * Quando o usuário entra ou sai, o React Navigation troca as telas sozinho.
 * Assim um comprador nunca consegue navegar para telas do vendedor (e vice-versa).
 */
export default function RootNavigator() {
  const { usuario, carregando } = useAuth();

  if (carregando) {
    return (
      <View style={estilos.carregando}>
        <ActivityIndicator size="large" color={cores.destaque} />
      </View>
    );
  }

  return (
    <Pilha.Navigator screenOptions={{ headerShown: false, contentStyle: { backgroundColor: cores.fundo } }}>
      {!usuario ? (
        <>
          <Pilha.Screen name="Login" component={LoginScreen} />
          <Pilha.Screen name="Cadastro" component={CadastroScreen} />
        </>
      ) : usuario.perfil === 'VENDEDOR' ? (
        <Pilha.Screen name="AreaVendedor" component={VendedorTabs} />
      ) : (
        <Pilha.Screen name="AreaComprador" component={CompradorTabs} />
      )}
    </Pilha.Navigator>
  );
}

const estilos = StyleSheet.create({
  carregando: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: cores.fundo },
});
