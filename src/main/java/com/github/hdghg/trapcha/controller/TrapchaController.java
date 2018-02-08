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

/**
 * This controller proxies all incoming requests to external resource
 * Intention of this is to check whether session valid and if not
 * redirect user to captcha page
 */
@RestController
@RequestMapping("s")
public class TrapchaController {

    private final WebClient webClient;
    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;
    private final ResponseEntity<Object> redirect = ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, "/trapcha")
            .build();

    public TrapchaController(SessionMetaReactiveRepository sessionMetaReactiveRepository) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.webClient = WebClient.create("http://jate.im");
    }

    /**
     * Maps /s/{fileId} requests to remote [GET] /{fileId} url pattern
     * If user cookie is not authorised then redirects on captcha page
     *
     * @param session Value of cookie "session"
     * @param fileId File subpath
     * @return File returned if cookie has access, redirect otherwise.
     */
    @RequestMapping(value = "/{fileId}")
    public Mono<ResponseEntity<Object>> getFile(@PathVariable String fileId,
            @CookieValue(name = "session", defaultValue = "") String session) {
        return sessionValid(session)
                .filter(v -> v)
                .flatMap(unbound -> webClient.get().uri(fileId).exchange())
                .flatMap(cr -> cr.toEntity(Object.class))
                .defaultIfEmpty(redirect);
    }

    /**
     * Detects if cookie valid based on quota argument. Quota is stored inside
     * database and decreased each time this method called.
     *
     * @param sessionGuid Session unique id
     * @return True when session valid, false otherwise.
     */
    private Mono<Boolean> sessionValid(String sessionGuid) {
        return sessionMetaReactiveRepository.findByGuid(sessionGuid)
                .map(sm -> new SessionMeta(sm.guid, sm.quota - 1).setId(sm.getId()))
                .flatMap(sessionMetaReactiveRepository::save)
                .map(sm -> sm.quota >= 0);
    }
}
