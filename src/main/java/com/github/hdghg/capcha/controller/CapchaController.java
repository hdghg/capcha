package com.github.hdghg.capcha.controller;

import com.github.hdghg.capcha.constants.Constants;
import com.github.hdghg.capcha.controller.redirect.Redirect;
import com.github.hdghg.capcha.dao.TileDao;
import com.github.hdghg.capcha.domain.SessionMeta;
import com.github.hdghg.capcha.domain.Task;
import com.github.hdghg.capcha.domain.Tile;
import com.github.hdghg.capcha.dto.CaptchaPage;
import com.github.hdghg.capcha.repository.SessionMetaReactiveRepository;
import com.github.hdghg.capcha.repository.TaskReactiveRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class CapchaController {

    private final SessionMetaReactiveRepository sessionMetaReactiveRepository;
    private final TaskReactiveRepository taskReactiveRepository;
    private final TileDao tileDao;
    private final Redirect redirect;

    public CapchaController(SessionMetaReactiveRepository sessionMetaReactiveRepository,
                            TaskReactiveRepository taskReactiveRepository,
                            TileDao tileDao,
                            Redirect redirect) {
        this.sessionMetaReactiveRepository = sessionMetaReactiveRepository;
        this.taskReactiveRepository = taskReactiveRepository;
        this.tileDao = tileDao;
        this.redirect = redirect;
    }

    /**
     * When this method is called, unique captcha task generated and passed to user
     *
     * @param fileId When user was redirected to this controller, this variable contains
     *               subpath user tried to access.
     * @return Returns model for view called "captchaPage"
     */
    @RequestMapping(Constants.CAPTCHA_PAGE)
    public Mono<Map<String, Object>> viewCaptchaPage(String fileId) {
        return tileDao.findSampleTiles()
                .collect(Collectors.toList())
                .flatMap(this::generateTask)
                .map(cp -> Map.of(Constants.FILE_ID, fileId,
                        Constants.TASK_ID, cp.taskId,
                        Constants.IMAGE_LIST, cp.imageList));
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
                .map(unbound -> new SessionMeta(UUID.randomUUID().toString(), 20))
                .flatMap(sessionMetaReactiveRepository::save)
                .map(sessionMeta -> redirect.toFile(sessionMeta.guid, fileId))
                .switchIfEmpty(redirect.toCaptchaPage(fileId));
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

}
