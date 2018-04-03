package com.github.hdghg.capcha.domain;

import java.util.Set;

/**
 * Entity that represents image as binary data and set of tags associated with the image
 */
public class Tile {

    private String id;
    public final byte[] image;
    public final Set<String> tags;

    public Tile(byte[] image, Set<String> tags) {
        this.image = image;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public Tile setId(String id) {
        this.id = id;
        return this;
    }
}
