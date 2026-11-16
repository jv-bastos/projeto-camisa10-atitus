package br.edu.atitus.camisa10.config;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.edu.atitus.camisa10.model.Categoria;
import br.edu.atitus.camisa10.model.Perfil;
import br.edu.atitus.camisa10.model.Produto;
import br.edu.atitus.camisa10.model.Tamanho;
import br.edu.atitus.camisa10.model.Usuario;
import br.edu.atitus.camisa10.repository.CategoriaRepository;
import br.edu.atitus.camisa10.repository.ProdutoRepository;
import br.edu.atitus.camisa10.repository.UsuarioRepository;
import br.edu.atitus.camisa10.service.ImagemService;
import br.edu.atitus.camisa10.util.AnoCamisa;

/**
 * Popula o banco com dados de exemplo na inicialização.
 * Só roda quando o banco está vazio, então não duplica dados no PostgreSQL.
 *
 * Usuários de teste (senha 123456):
 *   vendedor@camisa10.com  (VENDEDOR)
 *   comprador@camisa10.com (COMPRADOR)
 *
 * As fotos das camisas ficam em src/main/resources/seed/camisas e são copiadas
 * para a pasta de uploads, como se o vendedor tivesse enviado cada uma.
 */
@Component
@Order(2) // depois das MigracoesBanco
public class DataSeeder implements CommandLineRunner {

    public static final String SENHA_PADRAO = "123456";

    /** Categorias na ordem em que aparecem no app (RF09). */
    public static final List<String> CATEGORIAS = List.of(
            "Brasileirão", "Argentino", "Alemão", "Espanhol", "Francês",
            "Italiano", "Seleções", "Retrô", "Manga Longa", "Manga Longa Retrô");

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Dados de uma camisa de exemplo. As cores são usadas na personalização (nome/número). */
    private record Camisa(List<String> categorias, String nome, String descricao, String preco, int estoque,
                          String corPrincipal, String corTexto, String foto) {
    }

    private static final List<Camisa> CAMISAS = List.of(
            // --- Brasileirão ---
            torcedor("Brasileirão", "Grêmio", "26/27", "299.90", 20, "#0A6EB4", "#FFFFFF", "gremio-26-27.jpg"),
            torcedor("Brasileirão", "Internacional", "26/27", "299.90", 18, "#C8102E", "#FFFFFF", "internacional-26-27.jpg"),
            torcedor("Brasileirão", "Flamengo", "26/27", "299.90", 25, "#C8102E", "#FFFFFF", "flamengo-26-27.jpg"),
            torcedor("Brasileirão", "Corinthians", "26/27", "299.90", 22, "#FFFFFF", "#111111", "corinthians-26-27.jpg"),
            torcedor("Brasileirão", "Palmeiras", "26/27", "299.90", 15, "#006437", "#FFFFFF", "palmeiras-26-27.jpg"),
            // --- Argentino ---
            torcedor("Argentino", "Boca Juniors", "26/27", "279.90", 12, "#0B3D91", "#F2C200", "boca-juniors-26-27.jpg"),
            torcedor("Argentino", "River Plate", "26/27", "279.90", 12, "#D52B1E", "#FFFFFF", "river-plate-26-27.jpg"),
            torcedor("Argentino", "Racing", "26/27", "279.90", 8, "#75AADB", "#1A1A1A", "racing-26-27.jpg"),
            torcedor("Argentino", "Independiente", "25/26", "259.90", 6, "#D00000", "#FFFFFF", "independiente-25-26.jpg"),
            // --- Alemão ---
            torcedor("Alemão", "Bayern de Munique", "26/27", "349.90", 14, "#FFFFFF", "#DC052D", "bayern-de-munique-26-27.jpg"),
            torcedor("Alemão", "Borussia Dortmund", "26/27", "349.90", 10, "#6A3FB5", "#FDE100", "borussia-dortmund-26-27.jpg"),
            torcedor("Alemão", "Bayer Leverkusen", "24/25", "319.90", 7, "#E32221", "#000000", "bayer-leverkusen-24-25.jpg"),
            torcedor("Alemão", "RB Leipzig", "26/27", "329.90", 9, "#FFFFFF", "#DD0741", "red-bull-leipzig-26-27.jpg"),
            // --- Espanhol ---
            torcedor("Espanhol", "Barcelona", "26/27", "349.90", 16, "#A50044", "#EDBB00", "barcelona-26-27.jpg"),
            torcedor("Espanhol", "Real Madrid", "26/27", "349.90", 16, "#FFFFFF", "#1A1A1A", "real-madrid-26-27.jpg"),
            torcedor("Espanhol", "Atlético de Madrid", "26/27", "339.90", 10, "#FFFFFF", "#CB3524", "atletico-de-madrid-26-27.jpg"),
            torcedor("Espanhol", "Athletic Bilbao", "26/27", "329.90", 8, "#111111", "#FFFFFF", "atletico-bilbao-26-27.jpg"),
            // --- Francês ---
            torcedor("Francês", "PSG", "26/27", "349.90", 15, "#FFFFFF", "#004170", "psg-26-27.jpg"),
            torcedor("Francês", "Olympique de Marseille", "26/27", "329.90", 9, "#9BB7E8", "#1A1A1A", "olympique-de-marseille-26-27.jpg"),
            torcedor("Francês", "Lyon", "26/27", "319.90", 8, "#111111", "#FFFFFF", "lyon-26-27.jpg"),
            torcedor("Francês", "Mônaco", "26/27", "319.90", 7, "#CDEBDD", "#1A1A1A", "monaco-26-27.jpg"),
            // --- Italiano ---
            torcedor("Italiano", "Milan", "26/27", "349.90", 12, "#E21B23", "#FFFFFF", "milan-26-27.jpg"),
            torcedor("Italiano", "Inter de Milão", "26/27", "349.90", 12, "#5A5A5A", "#FFD100", "inter-de-milao-26-27.jpg"),
            torcedor("Italiano", "Juventus", "26/27", "349.90", 11, "#F4A7C3", "#111111", "juventus-26-27.jpg"),
            torcedor("Italiano", "Napoli", "26/27", "329.90", 9, "#D7262C", "#FFFFFF", "napoli-26-27.jpg"),
            // --- Seleções ---
            new Camisa(List.of("Seleções"), "Camisa Seleção Brasileira 26/27",
                    "A amarelinha da Copa do Mundo, com o escudo da CBF e as cinco estrelas.",
                    "399.90", 30, "#FFDC02", "#009C3B", "brasil-26-27.jpg"),
            selecao("Argentina", "379.90", 15, "#75AADB", "#111111", "argentina-26-27.jpg"),
            selecao("Alemanha", "379.90", 10, "#FFFFFF", "#111111", "alemanha-26-27.jpg"),
            selecao("Portugal", "379.90", 12, "#C8102E", "#FFFFFF", "portugal-26-27.jpg"),
            // --- Retrô ---
            retro(List.of("Retrô", "Seleções"), "Brasil", "2002", "259.90", 10, "#FFDC02", "#009C3B", "retro-brasil-2002.jpg"),
            retro(List.of("Retrô", "Seleções"), "Holanda", "1980", "259.90", 5, "#F36C21", "#000000", "retro-holanda-1980.jpg"),
            retro(List.of("Retrô", "Espanhol"), "Real Madrid", "2003", "259.90", 6, "#1C2541", "#FFFFFF", "retro-real-madrid-2003.jpg"),
            retro(List.of("Retrô", "Brasileirão"), "Corinthians", "2000", "259.90", 8, "#111111", "#FFFFFF", "retro-corinthians-2000.jpg"),
            // --- Manga Longa (torcedor) ---
            mangaLonga(List.of("Manga Longa", "Brasileirão"), "Cruzeiro", "26/27", "329.90", 8, "#FFFFFF", "#0033A0", "manga-longa-cruzeiro-26-27.jpg"),
            mangaLonga(List.of("Manga Longa"), "Benfica", "25/26", "319.90", 6, "#E30613", "#FFFFFF", "manga-longa-benfica-25-26.jpg"),
            mangaLonga(List.of("Manga Longa"), "Ajax", "22/23", "289.90", 5, "#1E2A5A", "#FFFFFF", "manga-longa-ajax-22-23.jpg"),
            mangaLonga(List.of("Manga Longa"), "Inter Miami", "26/27", "329.90", 7, "#111111", "#F7B5CD", "manga-longa-inter-de-miami-26-27.jpg"),
            // --- Manga Longa Retrô ---
            retro(List.of("Manga Longa Retrô", "Brasileirão"), "Internacional", "2006", "289.90", 6, "#C8102E", "#FFFFFF", "manga-longa-retro-internacional-2006.jpg"),
            retro(List.of("Manga Longa Retrô", "Seleções"), "Inglaterra", "1990", "289.90", 5, "#FFFFFF", "#1D2B5A", "manga-longa-retro-inglaterra-1990.jpg"),
            retro(List.of("Manga Longa Retrô", "Seleções"), "Alemanha", "90/91", "289.90", 4, "#1F9E6E", "#FFFFFF", "manga-longa-retro-alemanha-90-91.jpg"),
            retro(List.of("Manga Longa Retrô", "Italiano"), "Napoli", "98/99", "289.90", 4, "#FFFFFF", "#12A0D7", "manga-longa-retro-napoli-98-99.jpg"));

    /** Marca de cada camisa de exemplo, conferida pelo logo na foto (chave = arquivo da foto). */
    private static final Map<String, String> MARCAS = Map.ofEntries(
            Map.entry("gremio-26-27.jpg", "New Balance"),
            Map.entry("internacional-26-27.jpg", "Adidas"),
            Map.entry("flamengo-26-27.jpg", "Adidas"),
            Map.entry("corinthians-26-27.jpg", "Nike"),
            Map.entry("palmeiras-26-27.jpg", "Puma"),
            Map.entry("boca-juniors-26-27.jpg", "Adidas"),
            Map.entry("river-plate-26-27.jpg", "Adidas"),
            Map.entry("racing-26-27.jpg", "Nike"),
            Map.entry("independiente-25-26.jpg", "Puma"),
            Map.entry("bayern-de-munique-26-27.jpg", "Adidas"),
            Map.entry("borussia-dortmund-26-27.jpg", "Puma"),
            Map.entry("bayer-leverkusen-24-25.jpg", "Castore"),
            Map.entry("red-bull-leipzig-26-27.jpg", "Puma"),
            Map.entry("barcelona-26-27.jpg", "Nike"),
            Map.entry("real-madrid-26-27.jpg", "Adidas"),
            Map.entry("atletico-de-madrid-26-27.jpg", "Nike"),
            Map.entry("atletico-bilbao-26-27.jpg", "Castore"),
            Map.entry("psg-26-27.jpg", "Nike"),
            Map.entry("olympique-de-marseille-26-27.jpg", "Puma"),
            Map.entry("lyon-26-27.jpg", "Adidas"),
            Map.entry("monaco-26-27.jpg", "Kappa"),
            Map.entry("milan-26-27.jpg", "Puma"),
            Map.entry("inter-de-milao-26-27.jpg", "Nike"),
            Map.entry("juventus-26-27.jpg", "Adidas"),
            Map.entry("napoli-26-27.jpg", "EA7"),
            Map.entry("brasil-26-27.jpg", "Nike"),
            Map.entry("argentina-26-27.jpg", "Adidas"),
            Map.entry("alemanha-26-27.jpg", "Adidas"),
            Map.entry("portugal-26-27.jpg", "Puma"),
            Map.entry("retro-brasil-2002.jpg", "Nike"),
            Map.entry("retro-holanda-1980.jpg", "Adidas"),
            Map.entry("retro-real-madrid-2003.jpg", "Adidas"),
            Map.entry("retro-corinthians-2000.jpg", "Topper"),
            Map.entry("manga-longa-cruzeiro-26-27.jpg", "Adidas"),
            Map.entry("manga-longa-benfica-25-26.jpg", "Adidas"),
            Map.entry("manga-longa-ajax-22-23.jpg", "Adidas"),
            Map.entry("manga-longa-inter-de-miami-26-27.jpg", "Adidas"),
            Map.entry("manga-longa-retro-internacional-2006.jpg", "Reebok"),
            Map.entry("manga-longa-retro-inglaterra-1990.jpg", "Adidas"),
            Map.entry("manga-longa-retro-alemanha-90-91.jpg", "Adidas"),
            Map.entry("manga-longa-retro-napoli-98-99.jpg", "Nike"));

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImagemService imagemService;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      CategoriaRepository categoriaRepository,
                      ProdutoRepository produtoRepository,
                      PasswordEncoder passwordEncoder,
                      ImagemService imagemService) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
        this.passwordEncoder = passwordEncoder;
        this.imagemService = imagemService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            preencherNomeBuscaAntigo();
            log.info("Banco já possui dados, seed ignorado.");
            return;
        }

        // --- Usuários ---
        String hash = passwordEncoder.encode(SENHA_PADRAO);
        Usuario vendedor = usuarioRepository.save(
                new Usuario("Loja Camisa 10", "vendedor@camisa10.com", hash, Perfil.VENDEDOR));
        usuarioRepository.save(
                new Usuario("Comprador Teste", "comprador@camisa10.com", hash, Perfil.COMPRADOR));

        // --- Categorias (RF09) ---
        Map<String, Categoria> categorias = new LinkedHashMap<>();
        for (String nome : CATEGORIAS) {
            categorias.put(nome, categoriaRepository.save(new Categoria(nome)));
        }

        // --- Camisas com foto ---
        for (Camisa c : CAMISAS) {
            Produto produto = new Produto(c.nome(), c.descricao(), new BigDecimal(c.preco()), distribuir(c.estoque()),
                    c.corPrincipal(), c.corTexto(), c.categorias().stream().map(categorias::get).toList(), vendedor);
            produto.setImagemUrl(imagemService.copiarFotoDeExemplo(c.foto()));
            produto.setAno(AnoCamisa.extrair(c.nome()));
            produto.setMarca(MARCAS.get(c.foto()));
            produtoRepository.save(produto);
        }

        log.info("Seed concluído: {} usuários, {} categorias, {} produtos.",
                usuarioRepository.count(), categoriaRepository.count(), produtoRepository.count());
    }

    /**
     * Bancos criados por versões anteriores têm colunas novas vazias. Completa:
     * nome_busca (busca sem acentos), cor_familia (filtro por cor) e ano (pelo nome da camisa).
     */
    private void preencherNomeBuscaAntigo() {
        produtoRepository.findByNomeBuscaIsNullOrCorFamiliaIsNullOrAnoIsNull().forEach(p -> {
            p.setNome(p.getNome());
            p.setCorPrincipal(p.getCorPrincipal());
            if (p.getAno() == null) {
                p.setAno(AnoCamisa.extrair(p.getNome()));
            }
        });
    }

    /** Pesos da grade: mais unidades em M e G, menos nas pontas (P e XGG). */
    private static final Map<Tamanho, Integer> PESOS = new EnumMap<>(Map.of(
            Tamanho.P, 1, Tamanho.M, 3, Tamanho.G, 3, Tamanho.GG, 2, Tamanho.XGG, 1));
    private static final List<Tamanho> ORDEM_SOBRA = List.of(Tamanho.M, Tamanho.G, Tamanho.GG, Tamanho.P, Tamanho.XGG);

    /** Divide o total da camisa pelos tamanhos. Ex.: 20 → P 2, M 6, G 6, GG 4, XGG 2. */
    static Map<Tamanho, Integer> distribuir(int total) {
        int somaPesos = PESOS.values().stream().mapToInt(Integer::intValue).sum();
        Map<Tamanho, Integer> grade = new EnumMap<>(Tamanho.class);
        PESOS.forEach((t, peso) -> grade.put(t, total * peso / somaPesos));
        int sobra = total - grade.values().stream().mapToInt(Integer::intValue).sum();
        for (int i = 0; i < sobra; i++) {
            Tamanho t = ORDEM_SOBRA.get(i % ORDEM_SOBRA.size());
            grade.put(t, grade.get(t) + 1);
        }
        return grade;
    }

    // ----- atalhos para montar a lista sem repetir textos -----

    private static Camisa torcedor(String categoria, String time, String temporada, String preco, int estoque,
                                   String cor, String corTexto, String foto) {
        return new Camisa(List.of(categoria), "Camisa " + time + " " + temporada,
                "Camisa " + time + " modelo torcedor, temporada " + temporada + ".",
                preco, estoque, cor, corTexto, foto);
    }

    private static Camisa selecao(String pais, String preco, int estoque, String cor, String corTexto, String foto) {
        return new Camisa(List.of("Seleções"), "Camisa Seleção " + pais + " 26/27",
                "Camisa " + pais + " para a Copa do Mundo, modelo torcedor.",
                preco, estoque, cor, corTexto, foto);
    }

    /** A primeira categoria define o tipo ("Retrô" ou "Manga Longa Retrô"); as outras são extras. */
    private static Camisa retro(List<String> categorias, String time, String ano, String preco, int estoque,
                                String cor, String corTexto, String foto) {
        String tipo = categorias.get(0).equals("Retrô") ? "Camisa Retrô " : "Camisa Manga Longa Retrô ";
        return new Camisa(categorias, tipo + time + " " + ano,
                "Réplica da camisa clássica " + time + " " + ano + ".",
                preco, estoque, cor, corTexto, foto);
    }

    private static Camisa mangaLonga(List<String> categorias, String time, String temporada, String preco,
                                     int estoque, String cor, String corTexto, String foto) {
        return new Camisa(categorias, "Camisa Manga Longa " + time + " " + temporada,
                "Camisa " + time + " manga longa, modelo torcedor, temporada " + temporada + ".",
                preco, estoque, cor, corTexto, foto);
    }
}
