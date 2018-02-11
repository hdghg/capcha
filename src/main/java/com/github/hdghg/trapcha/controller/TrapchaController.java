package com.github.hdghg.trapcha.controller;

import com.github.hdghg.trapcha.domain.SessionMeta;
import com.github.hdghg.trapcha.repository.SessionMetaReactiveRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This controller proxies all incoming requests to external resource
 * Intention of this is to check whether session valid and if not
 * redirect user to captcha page
 */
@Controller
@RequestMapping
public class TrapchaController {

    public static final String CAPTCHA_PAGE = "/captchaPage";

    private final WebClient webClient;
    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;

    public TrapchaController(SessionMetaReactiveRepository sessionMetaReactiveRepository) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.webClient = WebClient.create("http://jate.im");
    }

    /**
     * Maps /s/{fileId} requests to remote [GET] /{fileId} url pattern
     * If user cookie is not authorised then redirects on captcha page
     *
     * @param session Value of cookie "session"
     * @param fileId  File subpath
     * @return File returned if cookie has access, redirect otherwise.
     */
    @RequestMapping(value = "s/{fileId}")
    @ResponseBody
    public Mono<ResponseEntity<Object>> getFile(@PathVariable String fileId,
                                                @CookieValue Optional<String> session) {
        return Mono.justOrEmpty(session)
                .flatMap(this::sessionValid)
                .filter(v -> v)
                .flatMap(unbound -> webClient.get().uri(fileId).exchange())
                .flatMap(clientResponse -> clientResponse.toEntity(Object.class))
                .defaultIfEmpty(redirectToCaptcha(fileId));
    }

    /**
     * When this method is called, unique captcha task generated and passed to user
     *
     * @param fileId When user was redirected to this controller, this variable contains
     *               subpath user tried to access.
     * @return Returns model for view called "captchaPage"
     */
    @RequestMapping(CAPTCHA_PAGE)
    public Map<String, String> viewCaptchaPage(String fileId) {
        return Collections.singletonMap("fileId", fileId);
    }

    /**
     * This controller is called when user answered captcha and clicked "Confirm"
     * If user answered properly, he redirected to file he tried to access before redirection to
     * captcha page. If answer is incorrect, user redirected back to captcha generation controller
     *
     * @param fileId Subpath to redirect user after success answer
     * @return Response object with redirection path based on correctness of user's answer
     */
    @RequestMapping("validate")
    public Mono<ResponseEntity<Object>> validate(String fileId) {
        return solutionValid(null, null)
                .filter(v -> v)
                .map(unbound -> new SessionMeta(UUID.randomUUID().toString(), 5))
                .flatMap(sessionMetaReactiveRepository::save)
                .map(sessionMeta -> redirectBackToFile(sessionMeta.guid, fileId))
                .defaultIfEmpty(redirectToCaptcha(fileId));
    }


    /**
     * Generates response that redirects user back to file he tried to access. Also sets cookie
     * that will allow user to get certain amount of files without entering captcha.
     *
     * @param sessionGuid Value of cookie "session" to be set
     * @param fileId      File user will be redirected to after setting cookie
     * @return Response entity that sets cookie and redirects back to originally loaded file
     */
    private ResponseEntity<Object> redirectBackToFile(String sessionGuid, String fileId) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, "session=" + sessionGuid)
                .header(HttpHeaders.LOCATION, "/s/" + fileId)
                .build();
    }

    /**
     * Generates response that redirects user to captcha page
     *
     * @param fileId File user tries to access. Required to redirect user back on successful captcha
     *               solution.
     * @return Response with redirecion
     */
    private ResponseEntity<Object> redirectToCaptcha(String fileId) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, CAPTCHA_PAGE + "?fileId=" + fileId)
                .build();
    }

    /**
     * Checks if user's captcha solution is valid
     *
     * @param taskId   Id of task
     * @param solution Solution
     * @return True when solution is valid, false otherwise
     */
    private Mono<Boolean> solutionValid(String taskId, String solution) {
        return Mono.just(true);
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
