package com.github.hdghg.capcha.dto;

import java.util.List;

/**
 * Abstraction over generated captcha page.
 */
public class CaptchaPage {

    public final String taskId;
    public final List<String> imageList;

    public CaptchaPage(String taskId, List<String> imageList) {
        this.taskId = taskId;
        this.imageList = imageList;
    }
}
