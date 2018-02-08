package com.github.hdghg.trapcha.domain;

import org.springframework.data.annotation.Id;

/**
 * Entity class that represents sessionMeta collection inside mongodb
 * Name is unique identifier of user session (stored in cookie). Quota has value of
 * how many times user with that cookie can access content without solving captcha.
 */
public class SessionMeta {

    @Id
    private String id;
    public final String guid;
    public final Integer quota;

    public SessionMeta(String guid, Integer quota) {
        this.guid = guid;
        this.quota = quota;
    }

    public String getId() {
        return id;
    }

    public SessionMeta setId(String id) {
        this.id = id;
        return this;
    }
}
