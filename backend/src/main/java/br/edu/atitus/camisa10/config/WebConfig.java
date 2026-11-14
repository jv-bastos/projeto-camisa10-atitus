package br.edu.atitus.camisa10.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.edu.atitus.camisa10.service.ImagemService;

/**
 * Serve os arquivos da pasta de uploads em GET /uploads/{arquivo}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImagemService imagemService;

    public WebConfig(ImagemService imagemService) {
        this.imagemService = imagemService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // toUri() gera "file:///C:/.../uploads/" (funciona no Windows, Linux e macOS)
        registry.addResourceHandler(ImagemService.PREFIXO_URL + "**")
                .addResourceLocations(imagemService.getPasta().toUri().toString());
    }
}
