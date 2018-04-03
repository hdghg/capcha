package com.github.hdghg.capcha.controller;

import com.github.hdghg.capcha.constants.Constants;
import com.github.hdghg.capcha.controller.redirect.Redirect;
import com.github.hdghg.capcha.domain.SessionMeta;
import com.github.hdghg.capcha.repository.SessionMetaReactiveRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * This controller proxies all incoming requests to external resource
 * Intention of this is to check whether session valid and if not
 * redirect user to captcha page
 */
@Controller
@RequestMapping
public class ProxyController {

    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;
    private final WebClient webClient;
    private final Redirect redirect;

    public ProxyController(Environment environment,
                           SessionMetaReactiveRepository sessionMetaReactiveRepository,
                           Redirect redirect) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.redirect = redirect;
        String guardedResource = environment.getProperty("guarded-resource");
        Objects.requireNonNull(guardedResource, "Guarded resource must be set");
        this.webClient = WebClient.create(guardedResource);
    }

    /**
     * Maps /s/{fileId} requests to remote [GET] /{fileId} url pattern
     * If user cookie is not authorised then redirects on captcha page
     *
     * @param session Value of cookie "session"
     * @param fileId  File subpath
     * @return File returned if cookie has access, redirect otherwise.
     */
    @RequestMapping(value = Constants.SHARED + "{" + Constants.FILE_ID + "}")
    @ResponseBody
    public Mono<ResponseEntity<Object>> getFile(@PathVariable String fileId,
                                                @CookieValue(required = false) String session) {
        return Mono.justOrEmpty(session)
                .flatMap(this::sessionValid)
                .filter(v -> v)
                .flatMap(unbound -> webClient.get().uri(fileId).exchange())
                .flatMap(clientResponse -> clientResponse.toEntity(Object.class))
                .defaultIfEmpty(redirect.toCaptchaPage(fileId));
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
                .doAfterSuccessOrError(this::decrementQuota)
                .map(sm -> sm.quota > 0);
    }

    /**
     * Decrement session's qouta. If quote equal or less then 1, then entire object is removed from
     * database. Actions are propagated to asynchronous thread.
     * @param sm SessionMeta whose quota to be decremented
     * @param throwable Ignored error
     */
    private void decrementQuota(SessionMeta sm, Throwable throwable) {
        if (null == sm) {
            return;
        }
        Mono<?> result;
        if (1 >= sm.quota) {
            result = sessionMetaReactiveRepository.delete(sm);
        } else {
            SessionMeta sessionMeta = new SessionMeta(sm.guid, sm.quota - 1)
                    .setId(sm.getId());
            result = sessionMetaReactiveRepository.save(sessionMeta);
        }
        result.subscribe();
    }
}
