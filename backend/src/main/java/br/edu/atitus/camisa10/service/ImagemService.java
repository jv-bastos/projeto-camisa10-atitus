package br.edu.atitus.camisa10.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.edu.atitus.camisa10.exception.RegraNegocioException;

/**
 * Grava as imagens dos produtos em uma pasta local (RF05).
 * Cada arquivo recebe um nome aleatório (UUID) para evitar conflitos e
 * nomes maliciosos. As imagens são servidas em GET /uploads/{arquivo} (ver WebConfig).
 */
@Service
public class ImagemService {

    public static final String PREFIXO_URL = "/uploads/";

    private static final Logger log = LoggerFactory.getLogger(ImagemService.class);

    /** Tipos aceitos e a extensão usada ao salvar. */
    private static final Map<String, String> TIPOS_ACEITOS = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path pasta;

    public ImagemService(@Value("${camisa10.upload.dir}") String diretorio) throws IOException {
        this.pasta = Paths.get(diretorio).toAbsolutePath().normalize();
        Files.createDirectories(pasta);
    }

    /** Salva a imagem e devolve a URL relativa (ex.: "/uploads/3f2a....jpg"). */
    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Selecione uma imagem.");
        }
        String extensao = TIPOS_ACEITOS.get(String.valueOf(arquivo.getContentType()).toLowerCase());
        if (extensao == null) {
            throw new RegraNegocioException("Formato de imagem não suportado. Use JPG, PNG ou WEBP.");
        }

        String nomeArquivo = UUID.randomUUID() + extensao;
        try (InputStream conteudo = arquivo.getInputStream()) {
            Files.copy(conteudo, pasta.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao salvar a imagem.", e);
        }
        return PREFIXO_URL + nomeArquivo;
    }

    /**
     * Copia uma foto que vem dentro do projeto (src/main/resources/seed/camisas) para a pasta
     * de uploads e devolve a URL dela. Usado pelo DataSeeder nas camisas de exemplo.
     * Retorna null se a foto não existir, para o seed continuar sem imagem.
     */
    public String copiarFotoDeExemplo(String arquivo) {
        ClassPathResource origem = new ClassPathResource("seed/camisas/" + arquivo);
        if (!origem.exists()) {
            log.warn("Foto de exemplo não encontrada: {}", arquivo);
            return null;
        }
        String nomeArquivo = "exemplo-" + arquivo;
        try (InputStream conteudo = origem.getInputStream()) {
            Files.copy(conteudo, pasta.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao copiar a foto de exemplo " + arquivo, e);
        }
        return PREFIXO_URL + nomeArquivo;
    }

    /** Copia uma imagem que já está no servidor (ex.: catálogo de fotos) para a pasta de uploads. */
    public String copiarArquivo(Path origem) {
        String nome = origem.getFileName().toString().toLowerCase();
        String extensao = nome.endsWith(".png") ? ".png" : nome.endsWith(".webp") ? ".webp" : ".jpg";
        String nomeArquivo = UUID.randomUUID() + extensao;
        try {
            Files.copy(origem, pasta.resolve(nomeArquivo), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao copiar a imagem.", e);
        }
        return PREFIXO_URL + nomeArquivo;
    }

    /** Apaga uma imagem antiga (usado quando o vendedor troca a foto). Falhas só vão para o log. */
    public void excluir(String url) {
        if (url == null || !url.startsWith(PREFIXO_URL)) {
            return;
        }
        Path arquivo = pasta.resolve(url.substring(PREFIXO_URL.length())).normalize();
        if (!arquivo.startsWith(pasta)) {
            return; // proteção: nunca apagar fora da pasta de uploads
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            log.warn("Não foi possível apagar a imagem antiga {}", arquivo, e);
        }
    }

    public Path getPasta() {
        return pasta;
    }
}
