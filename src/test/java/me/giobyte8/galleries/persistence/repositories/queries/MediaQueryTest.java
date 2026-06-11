package me.giobyte8.galleries.persistence.repositories.queries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaQueryTest {

    @Test
    void testDefaultQueries() {
        var mediaQuery = MediaQuery.builder().build();

        // Log them to stdout
        System.out.println("\n--- Query ---");
        System.out.println(mediaQuery.getQuery());
        System.out.println("--- Count query ---");
        System.out.println(mediaQuery.getCountQuery());
        System.out.println("---");

        assertNotNull(mediaQuery.getQuery());
        assertNotNull(mediaQuery.getCountQuery());
    }

}