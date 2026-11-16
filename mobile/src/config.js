/**
 * Endereço da API (backend Spring Boot). ÚNICO lugar do app onde a URL é definida.
 *
 * Qual valor usar?
 * - Celular físico com Expo Go: use o IP da sua máquina na rede local,
 *   por exemplo 'http://192.168.0.15:8080'. No Windows, descubra o IP com o
 *   comando `ipconfig` (campo "Endereço IPv4"). O celular e o computador
 *   precisam estar na MESMA rede Wi-Fi. "localhost" NÃO funciona no celular,
 *   porque para ele "localhost" é o próprio celular.
 * - Emulador Android: use 'http://10.0.2.2:8080'. O endereço 10.0.2.2 é como
 *   o emulador enxerga o "localhost" do computador.
 * - Simulador iOS: 'http://localhost:8080' funciona.
 *
 * Para não precisar editar este arquivo a cada rede, crie mobile/.env.local
 * (ignorado pelo git) com a linha abaixo e reinicie o `npx expo start`:
 *   EXPO_PUBLIC_API_URL=http://192.168.0.15:8080
 * Sem esse arquivo, vale o endereço padrão do emulador Android.
 */
export const API_URL = process.env.EXPO_PUBLIC_API_URL || 'http://10.0.2.2:8080';
