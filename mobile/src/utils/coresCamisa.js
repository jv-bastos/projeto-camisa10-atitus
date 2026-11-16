/** Cores oferecidas no cadastro (camisa e nome/número), com o nome mostrado ao usuário. */
export const PALETA = [
  { hex: '#FFFFFF', nome: 'Branco' },
  { hex: '#111111', nome: 'Preto' },
  { hex: '#C8102E', nome: 'Vermelho' },
  { hex: '#0A6EB4', nome: 'Azul' },
  { hex: '#006437', nome: 'Verde' },
  { hex: '#FFDC02', nome: 'Amarelo' },
  { hex: '#F36C21', nome: 'Laranja' },
  { hex: '#75AADB', nome: 'Azul-claro' },
  { hex: '#A50044', nome: 'Grená' },
  { hex: '#6A3FB5', nome: 'Roxo' },
  { hex: '#5A5A5A', nome: 'Cinza' },
  { hex: '#F4A7C3', nome: 'Rosa' },
];

function rgb(hex) {
  const valor = parseInt(String(hex).replace('#', ''), 16);
  return [(valor >> 16) & 255, (valor >> 8) & 255, valor & 255];
}

/**
 * Nome da cor para mostrar ao usuário: "#FFFFFF" → "Branco".
 * Cores fora da paleta (ex.: #CB3524 de uma camisa de exemplo) recebem o nome da
 * cor da paleta mais próxima — a mesma regra que o backend usa no filtro por cor.
 */
export function nomeDaCor(hex) {
  const codigo = String(hex).toUpperCase();
  const exata = PALETA.find((c) => c.hex === codigo);
  if (exata) return exata.nome;
  if (!/^#[0-9A-F]{6}$/.test(codigo)) return codigo;
  const [r, g, b] = rgb(codigo);
  let maisProxima = PALETA[0];
  let menorDistancia = Infinity;
  for (const cor of PALETA) {
    const [r2, g2, b2] = rgb(cor.hex);
    const distancia = (r - r2) ** 2 + (g - g2) ** 2 + (b - b2) ** 2;
    if (distancia < menorDistancia) {
      menorDistancia = distancia;
      maisProxima = cor;
    }
  }
  return maisProxima.nome;
}
