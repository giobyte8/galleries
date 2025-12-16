package me.giobyte8.galleries.scanner.services;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashingServiceTests {

    private final HashingService hashingSvc =
            new HashingService();

    @Test
    void hashTestPath() {
        String path = "testphotos/";
        String hash = "4d90b33ccdc779ff34c7c41a4bda0ce3cd79fe18cede202e96c45d9d17f668cf";

        assertThat(hashingSvc.hashPath(path)).isEqualTo(hash);
    }
}
