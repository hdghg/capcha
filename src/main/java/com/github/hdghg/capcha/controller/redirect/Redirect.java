package com.github.hdghg.capcha.controller.redirect;

import com.github.hdghg.capcha.constants.Constants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Redirection builders
 */
@Component
public class Redirect {

    /**
     * Generates response that redirects user back to file he tried to access. Also sets cookie
     * that will allow user to get certain amount of files without entering captcha.
     *
     * @param sessionGuid Value of cookie "session" to be set
     * @param fileId      File user will be redirected to after setting cookie
     * @return Response entity that sets cookie and redirects back to originally loaded file
     */
    public ResponseEntity<Object> toFile(String sessionGuid, String fileId) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, "session=" + sessionGuid)
                .header(HttpHeaders.LOCATION, Constants.SHARED + fileId)
                .build();
    }

    /**
     * Generates response that redirects user to captcha page
     *
     * @param fileId File user tries to access. Required to redirect user back on successful captcha
     *               solution.
     * @return Response with redirecion
     */
    public Mono<ResponseEntity<Object>> toCaptchaPage(String fileId) {
        return Mono.fromCallable(() -> {
            String location = Constants.CAPTCHA_PAGE + "?" + Constants.FILE_ID + "=" + fileId;
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, location)
                    .build();
        });
    }
}
