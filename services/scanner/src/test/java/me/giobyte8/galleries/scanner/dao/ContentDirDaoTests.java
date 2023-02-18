package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import me.giobyte8.galleries.scanner.services.HashingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(HashingService.class)
class ContentDirDaoTests {

    @Autowired
    private ContentDirDao contentDirDao;

    @Autowired
    private HashingService hashingService;

    private String testDirPath = "/pgalleries/portraits";

    @Test
    void injectedComponentsOk() {
        assertNotNull(contentDirDao);
        assertNotNull(hashingService);
    }

    @Test
    void basicCRUD() {
        ContentDir contentDir = new ContentDir();
        contentDir.setHashedPath(hashingService.hashPath(testDirPath));
        contentDir.setPath(testDirPath);
        contentDirDao.save(contentDir);

        ContentDir dbContentDir = contentDirDao
                .findById(hashingService.hashPath(testDirPath))
                .orElseThrow();
        assertEquals(dbContentDir.getHashedPath(), contentDir.getHashedPath());
    }
}
