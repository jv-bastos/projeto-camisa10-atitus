import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import EmConstrucaoScreen from '../screens/EmConstrucaoScreen';
import PerfilScreen from '../screens/comprador/PerfilScreen';
import { opcoesDaBarraDeAbas } from './tema';

const Abas = createBottomTabNavigator();

const ICONES = {
  Inicio: 'inicio',
  Carrinho: 'carrinho',
  Pedidos: 'pedidos',
  Perfil: 'perfil',
};

/** Área do COMPRADOR. Início, Carrinho e Pedidos entram na Fase 7. */
export default function CompradorTabs() {
  return (
    <Abas.Navigator screenOptions={opcoesDaBarraDeAbas(ICONES)}>
      <Abas.Screen
        name="Inicio"
        component={EmConstrucaoScreen}
        initialParams={{ fase: 'Fase 7' }}
        options={{ title: 'Início' }}
      />
      <Abas.Screen name="Carrinho" component={EmConstrucaoScreen} initialParams={{ fase: 'Fase 7' }} />
      <Abas.Screen name="Pedidos" component={EmConstrucaoScreen} initialParams={{ fase: 'Fase 7' }} />
      <Abas.Screen name="Perfil" component={PerfilScreen} />
    </Abas.Navigator>
  );
}
