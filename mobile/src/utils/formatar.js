/** Formatos de exibição usados nas telas. */

/** 299.9 → "R$ 299,90"; 1234.5 → "R$ 1.234,50". */
export function formatarPreco(valor) {
  return `R$ ${Number(valor).toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.')}`;
}

/** "2026-11-14T10:05:30.123" → "14/11/2026 às 10:05" (data/hora do servidor, sem fuso). */
export function formatarData(dataHora) {
  const [data, hora = ''] = String(dataHora).split('T');
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano} às ${hora.slice(0, 5)}`;
}

/** Status do pedido como aparece para o usuário. */
export const ROTULO_STATUS = {
  CONCLUIDO: 'Concluído',
  ENVIADO: 'Enviado',
  ENTREGUE: 'Entregue',
};
