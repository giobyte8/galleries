package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Neo4jDirectoryRepositoryTests {

    @Autowired
    private Neo4jDirectoryRepository dirRepository;

    @Test
    void saveAndFind() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .build();

        dirRepository.save(dir);
        Directory dirDb = dirRepository.findBy(path);

        assert dirDb.equals(dir);
    }

    @Test
    void update() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .recursive(true)
                .build();
        dirRepository.save(dir);

        // Update dir node
        int countBeforeUpdate = dirRepository.count();
        dir.setRecursive(false);
        dirRepository.save(dir);

        // Assert no new dir node was created
        int countAfterUpdate = dirRepository.count();
        assert countAfterUpdate == countBeforeUpdate;

        // Assert dir node was updated in database
        assert dir.equals(dirRepository.findBy(path));
    }
}
