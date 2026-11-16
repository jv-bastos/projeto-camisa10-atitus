package br.edu.atitus.camisa10.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.dto.pedido.ItemPedidoRequest;
import br.edu.atitus.camisa10.dto.pedido.PedidoRequest;
import br.edu.atitus.camisa10.dto.pedido.PedidoResponse;
import br.edu.atitus.camisa10.exception.RegraNegocioException;
import br.edu.atitus.camisa10.model.ItemPedido;
import br.edu.atitus.camisa10.model.Pedido;
import br.edu.atitus.camisa10.model.Personalizacao;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;
import br.edu.atitus.camisa10.repository.PedidoRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;

/**
 * Finalização da compra (RF13) e histórico do comprador (RF14).
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PersonalizacaoService personalizacaoService;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository,
                         UsuarioRepository usuarioRepository, PersonalizacaoService personalizacaoService) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.personalizacaoService = personalizacaoService;
    }

    /**
     * Cria o pedido com pagamento SIMULADO.
     *
     * É TRANSACIONAL: se qualquer item falhar (produto inexistente, sem estoque
     * ou personalização inválida), uma exceção é lançada e o Spring desfaz tudo:
     * nenhum pedido é gravado e nenhum estoque é baixado.
     *
     * Os preços vêm do banco, nunca do app, e o total é calculado aqui.
     */
    @Transactional
    public PedidoResponse criar(PedidoRequest dados, Long compradorId) {
        Pedido pedido = new Pedido(usuarioRepository.getReferenceById(compradorId));

        for (ItemPedidoRequest itemDados : dados.itens()) {
            Produto produto = produtoRepository.buscarParaCompra(itemDados.produtoId())
                    .filter(Produto::isAtivo)
                    .orElseThrow(() -> new RegraNegocioException(
                            "Produto " + itemDados.produtoId() + " não encontrado ou indisponível."));

            // 1) revalida a personalização (o app não é confiável)
            Personalizacao personalizacao = personalizacaoService.validar(itemDados.personalizacao());
            BigDecimal acrescimo = personalizacao == null ? BigDecimal.ZERO : personalizacaoService.getValorAcrescimo();

            // 2) confere e baixa o estoque DO TAMANHO escolhido. Se o mesmo produto/tamanho
            //    aparecer em duas linhas (personalizações diferentes), a segunda já enxerga
            //    o estoque reduzido.
            Tamanho tamanho = itemDados.tamanho();
            int quantidade = itemDados.quantidade();
            int disponivel = produto.getEstoque(tamanho);
            if (disponivel < quantidade) {
                throw new RegraNegocioException("Estoque insuficiente para \"" + produto.getNome()
                        + "\" (tamanho " + tamanho + "). Disponível: " + disponivel + ".");
            }
            produto.baixarEstoque(tamanho, quantidade);

            // 3) grava tamanho, preço e acréscimo do momento da compra; adicionarItem soma o total
            pedido.adicionarItem(new ItemPedido(produto, tamanho, quantidade, produto.getPreco(), acrescimo,
                    personalizacao));
        }

        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDoComprador(Long compradorId) {
        return pedidoRepository.findByCompradorIdOrderByDataCriacaoDescIdDesc(compradorId).stream()
                .map(PedidoResponse::de).toList();
    }
}
