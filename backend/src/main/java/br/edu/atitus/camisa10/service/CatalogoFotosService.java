package br.edu.atitus.camisa10.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.atitus.camisa10.dto.produto.CategoriaResponse;
import br.edu.atitus.camisa10.dto.produto.FotoCatalogoResponse;
import br.edu.atitus.camisa10.exception.RecursoNaoEncontradoException;
import br.edu.atitus.camisa10.model.Categoria;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.util.AnoCamisa;
import br.edu.atitus.camisa10.util.TextoBusca;

/**
 * Catálogo de fotos de camisas que acompanha o projeto (pasta backend/catalogo-fotos).
 * O vendedor escolhe uma foto no app em vez de fotografar a camisa.
 *
 * Estrutura da pasta:
 *   catalogo.json  -> [{ arquivo, nome, pasta, categorias: ["Retrô", "Seleções"] }]
 *   fotos/...      -> fotos de 640 px (copiadas para o produto)
 *   miniaturas/... -> fotos de 240 px (grade do app)
 * As fotos são servidas em GET /catalogo-fotos/** (ver WebConfig).
 */
@Service
public class CatalogoFotosService {

    public static final String PREFIXO_URL = "/catalogo-fotos/";

    private static final Logger log = LoggerFactory.getLogger(CatalogoFotosService.class);

    /** Linha do catalogo.json. nomeBusca é preenchido na leitura. */
    private record Foto(String arquivo, String nome, String pasta, List<String> categorias, String nomeBusca) {
    }

    private final Path pasta;
    private final List<Foto> fotos;
    private final CategoriaRepository categoriaRepository;
    private final ImagemService imagemService;

    public CatalogoFotosService(@Value("${camisa10.catalogo.dir}") String diretorio,
                                CategoriaRepository categoriaRepository,
                                ImagemService imagemService,
                                ObjectMapper objectMapper) {
        this.pasta = Paths.get(diretorio).toAbsolutePath().normalize();
        this.categoriaRepository = categoriaRepository;
        this.imagemService = imagemService;
        this.fotos = carregar(objectMapper);
    }

    private List<Foto> carregar(ObjectMapper objectMapper) {
        Path indice = pasta.resolve("catalogo.json");
        if (!Files.exists(indice)) {
            log.warn("Catálogo de fotos não encontrado em {}. O app mostrará o catálogo vazio.", indice);
            return List.of();
        }
        try {
            List<Map<String, Object>> linhas = objectMapper.readValue(indice.toFile(), new TypeReference<>() { });
            List<Foto> lidas = linhas.stream().map(l -> {
                String nome = (String) l.get("nome");
                @SuppressWarnings("unchecked")
                List<String> categorias = (List<String>) l.get("categorias");
                return new Foto((String) l.get("arquivo"), nome, (String) l.get("pasta"), categorias,
                        TextoBusca.normalizar(nome)); // a busca olha só o nome da camisa
            }).toList();
            log.info("Catálogo de fotos carregado: {} fotos.", lidas.size());
            return lidas;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler " + indice, e);
        }
    }

    /**
     * Fotos do catálogo, com filtro opcional por categoria sugerida e busca
     * (só no nome da camisa, sem diferenciar acentos/maiúsculas; a pasta não entra,
     * senão "brasil" traria todos os times da pasta "Brasileirão").
     */
    @Transactional(readOnly = true)
    public List<FotoCatalogoResponse> listar(Long categoriaId, String busca) {
        Map<String, Categoria> porNome = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(Categoria::getNome, Function.identity()));
        String nomeCategoria = categoriaId == null ? null
                : porNome.values().stream().filter(c -> c.getId().equals(categoriaId))
                        .map(Categoria::getNome).findFirst().orElse("");
        String termo = TextoBusca.normalizar(busca);

        return fotos.stream()
                .filter(f -> nomeCategoria == null || f.categorias().contains(nomeCategoria))
                .filter(f -> termo.isEmpty() || f.nomeBusca().contains(termo))
                .map(f -> new FotoCatalogoResponse(
                        f.arquivo(), f.nome(), f.pasta(), AnoCamisa.extrair(f.nome()),
                        f.categorias().stream().map(porNome::get).filter(Objects::nonNull)
                                .sorted(Comparator.comparing(Categoria::getId)) // mesma ordem do produto
                                .map(CategoriaResponse::de).toList(),
                        PREFIXO_URL + "miniaturas/" + f.arquivo(),
                        PREFIXO_URL + "fotos/" + f.arquivo()))
                .toList();
    }

    /**
     * Copia a foto escolhida para a pasta de uploads e devolve a URL dela.
     * Só aceita arquivos listados no catalogo.json (o app não consegue pedir outro caminho).
     */
    public String copiarParaUploads(String arquivo) {
        boolean existe = fotos.stream().anyMatch(f -> f.arquivo().equals(arquivo));
        Path origem = pasta.resolve("fotos").resolve(arquivo).normalize();
        if (!existe || !origem.startsWith(pasta) || !Files.exists(origem)) {
            throw new RecursoNaoEncontradoException("Foto do catálogo não encontrada.");
        }
        return imagemService.copiarArquivo(origem);
    }

    public Path getPasta() {
        return pasta;
    }
}
