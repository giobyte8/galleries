package me.giobyte8.galleries.scanner.services;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class FingerprintServiceTests {

    private final FingerprintService fingerprintSvc = new FingerprintService();

    @Test
    void fingerprintForKnownFile() throws IOException {
        Path absPath = Paths.get(
                "src/test/resources",
                "galleries",
                "cameras",
                "flower.webp"
        ).toAbsolutePath();

        var fp = fingerprintSvc.forPath(absPath);

        assertThat(fp.fileSize()).isNotNull().isPositive();
        assertThat(fp.lastModified()).isNotNull();
    }

    @Test
    void fingerprintShouldBeStableForSameFile() throws IOException {
        Path absPath = Paths.get(
                "src/test/resources",
                "galleries",
                "cameras",
                "flower.webp"
        ).toAbsolutePath();

        var fp1 = fingerprintSvc.forPath(absPath);
        var fp2 = fingerprintSvc.forPath(absPath);

        assertThat(fp2).isEqualTo(fp1);
    }
}


