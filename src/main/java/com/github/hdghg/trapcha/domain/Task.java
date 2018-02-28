package com.github.hdghg.trapcha.domain;

import java.util.Set;

/**
 * Abstraction over generated task. User must send correct answerSet to pass captcha
 */
public class Task {

    private String id;
    public final Set<Integer> answerSet;

    public Task(Set<Integer> answerSet) {
        this.answerSet = answerSet;
    }

    public String getId() {
        return id;
    }

    public Task setId(String id) {
        this.id = id;
        return this;
    }
}
