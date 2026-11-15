import { DMMono_400Regular } from '@expo-google-fonts/dm-mono/400Regular';
import { DMMono_500Medium } from '@expo-google-fonts/dm-mono/500Medium';
import { Manrope_300Light } from '@expo-google-fonts/manrope/300Light';
import { Manrope_500Medium } from '@expo-google-fonts/manrope/500Medium';
import { Manrope_700Bold } from '@expo-google-fonts/manrope/700Bold';
import { Manrope_800ExtraBold } from '@expo-google-fonts/manrope/800ExtraBold';
import { NavigationContainer } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { StatusBar } from 'expo-status-bar';
import { View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { AuthProvider } from './src/contexts/AuthContext';
import RootNavigator from './src/navigation/RootNavigator';
import { temaNavegacao } from './src/navigation/tema';
import { cores } from './src/theme';

/**
 * Raiz do app. Primeiro carrega as fontes (Manrope e DM Mono, embutidas no app,
 * funcionam sem internet); depois os "providers" deixam dados globais
 * disponíveis para todas as telas:
 * - SafeAreaProvider: medidas de notch/barras do sistema;
 * - AuthProvider: usuário logado e token (Context API).
 */
export default function App() {
  // os nomes das chaves são os usados em theme.js (fontes.*)
  const [fontesCarregadas, erroFontes] = useFonts({
    Manrope_300Light,
    Manrope_500Medium,
    Manrope_700Bold,
    Manrope_800ExtraBold,
    DMMono_400Regular,
    DMMono_500Medium,
  });

  // se as fontes falharem, o app segue com a fonte do sistema em vez de travar
  if (!fontesCarregadas && !erroFontes) {
    return <View style={{ flex: 1, backgroundColor: cores.fundo }} />;
  }

  return (
    <SafeAreaProvider>
      <AuthProvider>
        <NavigationContainer theme={temaNavegacao}>
          <RootNavigator />
          <StatusBar style="light" />
        </NavigationContainer>
      </AuthProvider>
    </SafeAreaProvider>
  );
}
