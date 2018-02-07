package com.github.hdghg.trapcha.domain;

public class SessionMeta {

    private String id;
    private String name;
    private Integer quota;

    public SessionMeta() {
    }

    public SessionMeta(String id, String name, Integer quota) {
        this.id = id;
        this.name = name;
        this.quota = quota;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }
}
