import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import EmConstrucaoScreen from '../screens/EmConstrucaoScreen';
import { opcoesDaBarraDeAbas } from './tema';

const Abas = createBottomTabNavigator();

const ICONES = {
  Dashboard: 'dashboard',
  Produtos: 'camisa',
  Vendas: 'vendas',
};

/** Área do VENDEDOR. As telas reais entram na Fase 6. */
export default function VendedorTabs() {
  return (
    <Abas.Navigator screenOptions={opcoesDaBarraDeAbas(ICONES)}>
      <Abas.Screen name="Dashboard" component={EmConstrucaoScreen} initialParams={{ fase: 'Fase 6' }} />
      <Abas.Screen name="Produtos" component={EmConstrucaoScreen} initialParams={{ fase: 'Fase 6' }} />
      <Abas.Screen name="Vendas" component={EmConstrucaoScreen} initialParams={{ fase: 'Fase 6' }} />
    </Abas.Navigator>
  );
}
