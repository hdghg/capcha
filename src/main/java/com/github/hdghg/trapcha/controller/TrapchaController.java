package com.github.hdghg.trapcha.controller;

import com.github.hdghg.trapcha.dao.TileDao;
import com.github.hdghg.trapcha.domain.SessionMeta;
import com.github.hdghg.trapcha.domain.Task;
import com.github.hdghg.trapcha.domain.Tile;
import com.github.hdghg.trapcha.dto.CaptchaPage;
import com.github.hdghg.trapcha.repository.SessionMetaReactiveRepository;
import com.github.hdghg.trapcha.repository.TaskReactiveRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller proxies all incoming requests to external resource
 * Intention of this is to check whether session valid and if not
 * redirect user to captcha page
 */
@Controller
@RequestMapping
public class TrapchaController {

    public static final String CAPTCHA_PAGE = "/captchaPage";
    public static final String FILE_ID = "fileId";
    public static final String TASK_ID = "taskId";
    public static final String IMAGE_LIST = "imageList";
    public static final String SHARED = "/s/";

    private final WebClient webClient;
    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final TileDao tileDao;

    public TrapchaController(Environment environment,
                             SessionMetaReactiveRepository sessionMetaReactiveRepository,
                             TaskReactiveRepository taskReactiveRepository,
                             TileDao tileDao) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.taskReactiveRepository = taskReactiveRepository;
        this.tileDao = tileDao;
        String guardedResource = environment.getProperty("guarded-resource");
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
    @RequestMapping(value = SHARED + "{" + FILE_ID + "}")
    @ResponseBody
    public Mono<ResponseEntity<Object>> getFile(@PathVariable String fileId,
                                                @CookieValue(required = false) String session) {
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
    public Mono<Map<String, Object>> viewCaptchaPage(String fileId) {
        return tileDao.findSampleTiles()
                .collect(Collectors.toList())
                .flatMap(this::generateTask)
                .map(cp -> Map.of(FILE_ID, fileId,
                        TASK_ID, cp.taskId,
                        IMAGE_LIST, cp.imageList));
    }

    /**
     * Accepts set of {@link Tile} abstractions and generates task + answer for given
     * tile list. Answer is saved to database, while task is wrapped to {@link CaptchaPage}
     *
     * @param tileList List of 9 tiles
     * @return Generated captcha page abstraction
     */
    private Mono<CaptchaPage> generateTask(List<Tile> tileList) {
        List<String> imageList = new ArrayList<>();
        Set<Integer> answer = new HashSet<>();
        for (int i = 0; i < tileList.size(); i++) {
            Tile tile = tileList.get(i);
            imageList.add(Base64Utils.encodeToString(tile.image));
            if (tile.tags.contains("girl")) {
                answer.add(i + 1);
            }
        }
        return taskReactiveRepository.save(new Task(answer))
                .map(t -> new CaptchaPage(t.getId(), imageList));
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
    public Mono<ResponseEntity<Object>> validate(String fileId, String taskId, Integer[] answer) {
        return answerValid(taskId, answer)
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
                .header(HttpHeaders.LOCATION, SHARED + fileId)
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
                .header(HttpHeaders.LOCATION, CAPTCHA_PAGE + "?" + FILE_ID + "=" + fileId)
                .build();
    }

    /**
     * Checks if user's captcha answer is valid
     *
     * @param taskId Id of task
     * @param answer Solution
     * @return True when answer is valid, false otherwise
     */
    private Mono<Boolean> answerValid(String taskId, Integer[] answer) {
        return taskReactiveRepository.findById(taskId)
                .filter(t -> t.answerSet.equals(Set.of(answer)))
                .map(t -> true);
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
