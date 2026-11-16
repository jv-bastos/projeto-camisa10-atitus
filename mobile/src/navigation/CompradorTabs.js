import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import { useCarrinho } from '../contexts/CarrinhoContext';
import CarrinhoScreen from '../screens/comprador/CarrinhoScreen';
import PedidosScreen from '../screens/comprador/PedidosScreen';
import PerfilScreen from '../screens/PerfilScreen';
import VitrineScreen from '../screens/comprador/VitrineScreen';
import { opcoesDaBarraDeAbas } from './tema';

const Abas = createBottomTabNavigator();

const ICONES = {
  Inicio: 'inicio',
  Carrinho: 'carrinho',
  Pedidos: 'pedidos',
  Perfil: 'perfil',
};

/** Área do COMPRADOR. A aba Carrinho mostra quantas camisas há nele. */
export default function CompradorTabs() {
  const { totalItens } = useCarrinho();

  return (
    <Abas.Navigator screenOptions={opcoesDaBarraDeAbas(ICONES)}>
      <Abas.Screen name="Inicio" component={VitrineScreen} options={{ title: 'Início' }} />
      <Abas.Screen
        name="Carrinho"
        component={CarrinhoScreen}
        options={{ tabBarBadge: totalItens > 0 ? totalItens : undefined }}
      />
      <Abas.Screen name="Pedidos" component={PedidosScreen} />
      <Abas.Screen name="Perfil" component={PerfilScreen} />
    </Abas.Navigator>
  );
}
