/**
 * Lê o preço digitado: "299,90", "1.299,90", "R$299,90" ou "299.90" → número.
 * Com vírgula, o ponto é separador de milhar; sem vírgula, o ponto é o decimal
 * (alguns teclados Android só mostram ponto). Texto inválido → NaN.
 */
export function lerPreco(texto) {
  const limpo = String(texto ?? '').replace(/[R$\s]/g, '');
  if (!limpo) return NaN;
  const normalizado = limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo;
  return /^\d+(\.\d{1,2})?$/.test(normalizado) ? Number(normalizado) : NaN;
}
