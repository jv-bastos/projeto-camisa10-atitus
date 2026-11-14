package br.edu.atitus.camisa10.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.camisa10.dto.ConfigPersonalizacaoResponse;
import br.edu.atitus.camisa10.service.PersonalizacaoService;

/** Configurações públicas lidas pelo app. */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final PersonalizacaoService personalizacaoService;

    public ConfigController(PersonalizacaoService personalizacaoService) {
        this.personalizacaoService = personalizacaoService;
    }

    /** GET /api/config/personalizacao → { valorAcrescimo, maxCaracteresNome } */
    @GetMapping("/personalizacao")
    public ConfigPersonalizacaoResponse personalizacao() {
        return new ConfigPersonalizacaoResponse(
                personalizacaoService.getValorAcrescimo(),
                personalizacaoService.getMaxCaracteresNome());
    }
}
