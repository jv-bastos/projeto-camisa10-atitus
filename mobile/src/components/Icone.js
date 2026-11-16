import Svg, { Circle, Path, Rect } from 'react-native-svg';

import { cores } from '../theme';

/**
 * Ícones de traço desenhados em SVG (grade 24x24), para não depender de
 * biblioteca de ícones. Uso: <Icone nome="carrinho" cor={cores.destaque} tamanho={24} />
 */
const DESENHOS = {
  inicio: <Path d="M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3z" />,
  carrinho: (
    <>
      <Path d="M2 3h3l2.6 12.2a1 1 0 0 0 1 .8h9.8a1 1 0 0 0 1-.8L21 7H6" />
      <Circle cx="9" cy="20" r="1.5" />
      <Circle cx="18" cy="20" r="1.5" />
    </>
  ),
  pedidos: <Path d="M6 2h12v20l-3-2-3 2-3-2-3 2zM9 7h6M9 11h6M9 15h4" />,
  perfil: (
    <>
      <Circle cx="12" cy="8" r="4" />
      <Path d="M4 21c0-4.4 3.6-8 8-8s8 3.6 8 8" />
    </>
  ),
  dashboard: <Path d="M3 21h18M6 17v-6M12 17V7M18 17v-3" />,
  camisa: <Path d="M8 3 4 5 2 10l3 1.5V21h14v-9.5L22 10l-2-5-4-2c0 2.2-1.8 3-4 3S8 5.2 8 3z" />,
  vendas: (
    <>
      <Circle cx="12" cy="12" r="9" />
      <Path d="M15 9.5c-.5-1-1.6-1.5-3-1.5-1.7 0-3 .9-3 2s1.3 1.6 3 2 3 .9 3 2-1.3 2-3 2c-1.4 0-2.5-.5-3-1.5M12 6v2M12 16v2" />
    </>
  ),
  mais: <Path d="M12 5v14M5 12h14" />,
  filtro: (
    <>
      <Path d="M4 6h16M4 12h16M4 18h16" />
      <Circle cx="9" cy="6" r="2" />
      <Circle cx="15" cy="12" r="2" />
      <Circle cx="7" cy="18" r="2" />
    </>
  ),
  menos: <Path d="M5 12h14" />,
  lixeira: <Path d="M4 7h16M10 11v6M14 11v6M6 7l1 13h10l1-13M9 7V4h6v3" />,
  editar: <Path d="M4 20h4L19 9l-4-4L4 16v4z" />,
  camera: (
    <>
      <Path d="M3 8h4l2-3h6l2 3h4v12H3z" />
      <Circle cx="12" cy="13.5" r="3.5" />
    </>
  ),
  imagem: (
    <>
      <Rect x="3" y="3" width="18" height="18" rx="2" />
      <Circle cx="8.5" cy="8.5" r="1.5" />
      <Path d="M21 15l-5-5L5 21" />
    </>
  ),
  sair: <Path d="M15 4h4v16h-4M10 17l5-5-5-5M15 12H3" />,
  busca: (
    <>
      <Circle cx="11" cy="11" r="7" />
      <Path d="M20 20l-4-4" />
    </>
  ),
  voltar: <Path d="M15 18l-6-6 6-6" />,
  check: <Path d="M5 12l5 5L20 7" />,
};

export default function Icone({ nome, tamanho = 24, cor = cores.texto, espessura = 2 }) {
  return (
    <Svg
      width={tamanho}
      height={tamanho}
      viewBox="0 0 24 24"
      fill="none"
      stroke={cor}
      strokeWidth={espessura}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      {DESENHOS[nome]}
    </Svg>
  );
}
