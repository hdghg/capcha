package com.github.hdghg.trapcha.controller;

import com.github.hdghg.trapcha.domain.SessionMeta;
import com.github.hdghg.trapcha.repository.SessionMetaReactiveRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("s")
public class TrapchaController {

    private final WebClient webClient;
    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;

    public TrapchaController(SessionMetaReactiveRepository sessionMetaReactiveRepository) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.webClient = WebClient.create("http://jate.im");
    }

    private ResponseEntity<Object> redirect = ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, "/trapcha").build();

    @RequestMapping(value = "/{fileId}")
    public Mono<ResponseEntity<Object>> home(@CookieValue(name = "session", defaultValue = "") String session,
                                             @PathVariable String fileId) {
        return sessionValid(session)
                .filter(v -> v)
                .flatMap(unbound -> webClient.get().uri(fileId).exchange())
                .flatMap(cr -> cr.toEntity(Object.class))
                .defaultIfEmpty(redirect);
    }

    private Mono<Boolean> sessionValid(String session) {
        return sessionMetaReactiveRepository.findByName(session)
                .map(SessionMeta::getQuota)
                .map(q -> q > 0);
    }
}
