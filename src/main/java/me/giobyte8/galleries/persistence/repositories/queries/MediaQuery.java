package me.giobyte8.galleries.persistence.repositories.queries;

import lombok.Getter;

public class MediaQuery {

    @Getter
    private final String query;

    @Getter
    private final String countQuery;

    protected MediaQuery(String query, String countQuery) {
        this.query = query;
        this.countQuery = countQuery;
    }

    public static MediaQueryBuilder builder() {
        return new MediaQueryBuilder();
    }
}
