package br.edu.atitus.camisa10.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.edu.atitus.camisa10.service.CatalogoFotosService;
import br.edu.atitus.camisa10.service.ImagemService;

/**
 * Serve os arquivos estáticos:
 * - GET /uploads/{arquivo}         -> fotos enviadas pelos vendedores;
 * - GET /catalogo-fotos/{caminho}  -> catálogo de fotos que acompanha o projeto.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImagemService imagemService;
    private final CatalogoFotosService catalogoFotosService;

    public WebConfig(ImagemService imagemService, CatalogoFotosService catalogoFotosService) {
        this.imagemService = imagemService;
        this.catalogoFotosService = catalogoFotosService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // toUri() gera "file:///C:/.../uploads/" (funciona no Windows, Linux e macOS)
        registry.addResourceHandler(ImagemService.PREFIXO_URL + "**")
                .addResourceLocations(imagemService.getPasta().toUri().toString());
        registry.addResourceHandler(CatalogoFotosService.PREFIXO_URL + "**")
                .addResourceLocations(catalogoFotosService.getPasta().toUri().toString());
    }
}
