import { useEffect, useState } from 'react';
import { Modal, Pressable, ScrollView, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';

import { cores, espacos, fontes, raios, tipografia } from '../theme';
import { PALETA } from '../utils/coresCamisa';
import Botao from './Botao';
import Chip from './Chip';
import Icone from './Icone';
import TituloTela from './TituloTela';

/** Filtros sem nada escolhido (ordenação padrão: nome A–Z). */
export const FILTROS_VAZIOS = {
  ordenar: 'nome',
  precoMin: '',
  precoMax: '',
  anoMin: '',
  anoMax: '',
  cores: [],
  marcas: [],
  tamanho: null,
};

/** Quantos filtros estão ativos (para o número no botão "Filtrar"). A ordenação não conta. */
export function contarFiltrosAtivos(f) {
  return (
    (f.precoMin || f.precoMax ? 1 : 0) +
    (f.anoMin || f.anoMax ? 1 : 0) +
    (f.cores.length ? 1 : 0) +
    (f.marcas.length ? 1 : 0) +
    (f.tamanho ? 1 : 0)
  );
}

/** "R$ 299,90" → "299.90" (o backend recebe ponto como decimal). */
function precoParaApi(texto) {
  const limpo = String(texto).replace(/[^\d,.]/g, '');
  return limpo.includes(',') ? limpo.replace(/\./g, '').replace(',', '.') : limpo;
}

/** Converte os valores do painel para os parâmetros de GET /api/produtos. */
export function filtrosParaApi(f) {
  return {
    ordenar: f.ordenar,
    precoMin: precoParaApi(f.precoMin),
    precoMax: precoParaApi(f.precoMax),
    anoMin: f.anoMin,
    anoMax: f.anoMax,
    cores: f.cores,
    marcas: f.marcas,
    tamanho: f.tamanho,
  };
}

/**
 * Painel "Filtrar e ordenar" da vitrine. Edita uma cópia dos filtros e só
 * devolve ao tocar em "Aplicar". "opcoes" vem de GET /api/produtos/filtros.
 */
export default function FiltrosVitrine({ visivel, opcoes, valores, onAplicar, onFechar }) {
  const [rascunho, setRascunho] = useState(valores);

  // ao abrir, começa pelos filtros que estão valendo
  useEffect(() => {
    if (visivel) setRascunho(valores);
  }, [visivel, valores]);

  const atualizar = (campo, valor) => setRascunho((atual) => ({ ...atual, [campo]: valor }));
  const alternarNaLista = (campo, item) =>
    setRascunho((atual) => ({
      ...atual,
      [campo]: atual[campo].includes(item) ? atual[campo].filter((x) => x !== item) : [...atual[campo], item],
    }));
  const somenteNumeros = (texto) => texto.replace(/\D/g, '');

  const coresDisponiveis = PALETA.filter((c) => opcoes?.cores?.includes(c.nome));
  const exemploPrecoMin = opcoes?.precoMin ? `Ex: ${Math.floor(opcoes.precoMin)}` : 'Mín.';
  const exemploPrecoMax = opcoes?.precoMax ? `Ex: ${Math.ceil(opcoes.precoMax)}` : 'Máx.';

  return (
    <Modal visible={visivel} animationType="slide" presentationStyle="pageSheet" onRequestClose={onFechar}>
      <SafeAreaProvider>
        <SafeAreaView style={estilos.tela} edges={['top', 'bottom']}>
          <View style={estilos.topo}>
            <TituloTela rotulo="Vitrine" fino="Filtrar e" forte="ordenar" />
            <Pressable onPress={onFechar} accessibilityRole="button" accessibilityLabel="Fechar" hitSlop={10} style={estilos.fechar}>
              <Icone nome="mais" cor={cores.texto} tamanho={26} espessura={2} />
            </Pressable>
          </View>

          <ScrollView contentContainerStyle={estilos.conteudo} keyboardShouldPersistTaps="handled">
            <Secao titulo="Ordenar por">
              <View style={estilos.chips}>
                {(opcoes?.ordenacoes ?? []).map((o) => (
                  <Chip key={o.valor} rotulo={o.rotulo} ativo={rascunho.ordenar === o.valor} onPress={() => atualizar('ordenar', o.valor)} />
                ))}
              </View>
            </Secao>

            <Secao titulo="Preço (R$)">
              <View style={estilos.faixa}>
                <CampoFaixa valor={rascunho.precoMin} onChange={(t) => atualizar('precoMin', t)} placeholder={exemploPrecoMin} decimal rotulo="Preço mínimo" />
                <Text style={estilos.ate}>até</Text>
                <CampoFaixa valor={rascunho.precoMax} onChange={(t) => atualizar('precoMax', t)} placeholder={exemploPrecoMax} decimal rotulo="Preço máximo" />
              </View>
            </Secao>

            <Secao titulo="Ano">
              <View style={estilos.faixa}>
                <CampoFaixa
                  valor={rascunho.anoMin}
                  onChange={(t) => atualizar('anoMin', somenteNumeros(t))}
                  placeholder={opcoes?.anoMin ? `Ex: ${opcoes.anoMin}` : 'De'}
                  rotulo="Ano inicial"
                />
                <Text style={estilos.ate}>até</Text>
                <CampoFaixa
                  valor={rascunho.anoMax}
                  onChange={(t) => atualizar('anoMax', somenteNumeros(t))}
                  placeholder={opcoes?.anoMax ? `Ex: ${opcoes.anoMax}` : 'Até'}
                  rotulo="Ano final"
                />
              </View>
            </Secao>

            <Secao titulo="Tamanho disponível">
              <View style={estilos.chips}>
                {(opcoes?.tamanhos ?? []).map((t) => (
                  <Chip key={t} rotulo={t} ativo={rascunho.tamanho === t} onPress={() => atualizar('tamanho', rascunho.tamanho === t ? null : t)} />
                ))}
              </View>
            </Secao>

            <Secao titulo="Cor da camisa">
              <View style={estilos.cores}>
                {coresDisponiveis.map((c) => {
                  const ativa = rascunho.cores.includes(c.nome);
                  return (
                    <Pressable
                      key={c.nome}
                      onPress={() => alternarNaLista('cores', c.nome)}
                      accessibilityRole="checkbox"
                      accessibilityState={{ checked: ativa }}
                      accessibilityLabel={c.nome}
                      style={estilos.opcaoCor}
                    >
                      <View style={[estilos.anel, ativa && estilos.anelAtivo]}>
                        <View style={[estilos.amostra, { backgroundColor: c.hex }]} />
                      </View>
                      <Text style={[estilos.nomeCor, ativa && estilos.nomeCorAtivo]}>{c.nome}</Text>
                    </Pressable>
                  );
                })}
              </View>
            </Secao>

            <Secao titulo="Marca">
              <View style={estilos.chips}>
                {(opcoes?.marcas ?? []).map((m) => (
                  <Chip key={m} rotulo={m} ativo={rascunho.marcas.includes(m)} onPress={() => alternarNaLista('marcas', m)} />
                ))}
              </View>
            </Secao>
          </ScrollView>

          <View style={estilos.rodape}>
            <Botao titulo="Aplicar" onPress={() => onAplicar(rascunho)} />
            <Botao titulo="Limpar tudo" variante="link" onPress={() => setRascunho(FILTROS_VAZIOS)} />
          </View>
        </SafeAreaView>
      </SafeAreaProvider>
    </Modal>
  );
}

function Secao({ titulo, children }) {
  return (
    <View style={estilos.secao}>
      <Text style={estilos.rotulo}>{titulo}</Text>
      {children}
    </View>
  );
}

function CampoFaixa({ valor, onChange, placeholder, decimal = false, rotulo }) {
  return (
    <TextInput
      value={valor}
      onChangeText={onChange}
      placeholder={placeholder}
      placeholderTextColor={cores.textoSecundario}
      keyboardType={decimal ? 'decimal-pad' : 'number-pad'}
      maxLength={decimal ? 9 : 4}
      selectionColor={cores.destaque}
      style={estilos.campoFaixa}
      accessibilityLabel={rotulo}
    />
  );
}

const estilos = StyleSheet.create({
  tela: { flex: 1, backgroundColor: cores.fundo },
  topo: { flexDirection: 'row', alignItems: 'flex-start', justifyContent: 'space-between', padding: espacos.lg, paddingBottom: 0 },
  fechar: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center', transform: [{ rotate: '45deg' }] },
  conteudo: { padding: espacos.lg, gap: espacos.lg },
  secao: { gap: espacos.sm + 2 },
  rotulo: { ...tipografia.rotulo, color: cores.textoSecundario },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: espacos.sm },
  faixa: { flexDirection: 'row', alignItems: 'center', gap: espacos.md },
  ate: { ...tipografia.pequeno, color: cores.textoSecundario },
  campoFaixa: {
    flex: 1,
    height: 48,
    borderWidth: 1,
    borderColor: cores.bordaForte,
    borderRadius: raios.sm,
    paddingHorizontal: espacos.md - 4,
    fontFamily: fontes.monoMedia,
    fontSize: 16,
    color: cores.texto,
  },
  cores: { flexDirection: 'row', flexWrap: 'wrap', gap: espacos.sm },
  opcaoCor: { width: 64, alignItems: 'center', gap: 4 },
  anel: {
    width: 44,
    height: 44,
    borderRadius: raios.redondo,
    borderWidth: 2,
    borderColor: 'transparent',
    alignItems: 'center',
    justifyContent: 'center',
  },
  anelAtivo: { borderColor: cores.destaque },
  amostra: { width: 32, height: 32, borderRadius: raios.redondo, borderWidth: 1, borderColor: cores.bordaForte },
  nomeCor: { fontFamily: fontes.media, fontSize: 11, color: cores.textoSecundario, textAlign: 'center' },
  nomeCorAtivo: { color: cores.destaque },
  rodape: { paddingHorizontal: espacos.lg, paddingTop: espacos.sm, paddingBottom: espacos.md, gap: espacos.xs },
});
