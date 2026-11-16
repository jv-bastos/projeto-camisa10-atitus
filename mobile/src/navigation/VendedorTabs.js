import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import PerfilScreen from '../screens/PerfilScreen';
import MeusProdutosScreen from '../screens/vendedor/MeusProdutosScreen';
import PainelScreen from '../screens/vendedor/PainelScreen';
import VendasScreen from '../screens/vendedor/VendasScreen';
import { opcoesDaBarraDeAbas } from './tema';

const Abas = createBottomTabNavigator();

const ICONES = {
  Painel: 'dashboard',
  Produtos: 'camisa',
  Vendas: 'vendas',
  Perfil: 'perfil',
};

/** Área do VENDEDOR: painel (RF06), produtos (RF04/RF05/RF08), vendas (RF07) e perfil (logout). */
export default function VendedorTabs() {
  return (
    <Abas.Navigator screenOptions={opcoesDaBarraDeAbas(ICONES)}>
      <Abas.Screen name="Painel" component={PainelScreen} />
      <Abas.Screen name="Produtos" component={MeusProdutosScreen} />
      <Abas.Screen name="Vendas" component={VendasScreen} />
      <Abas.Screen name="Perfil" component={PerfilScreen} />
    </Abas.Navigator>
  );
}
