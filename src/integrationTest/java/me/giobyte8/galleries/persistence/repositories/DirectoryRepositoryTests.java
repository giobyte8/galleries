package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Import({
        DirRowMapper.class,
        ImgRowMapper.class,
})
class DirectoryRepositoryTests extends BaseIntegrationTest {

    @Autowired
    private DirectoryRepository dirRepository;

    @Test
    void findNonExistent() {
        Directory dir = dirRepository.findByPath("non/existent/dir");
        assertNull(dir);
    }

    @Test
    void saveAndFind() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .build();

        dirRepository.save(dir);
        Directory dirDb = dirRepository.findById(path).orElseThrow();

        assert dirDb.equals(dir);
    }

    @Test
    void saveAndFindByPath() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .build();

        dirRepository.save(dir);
        Directory dirDb = dirRepository.findByPath(path);

        assert dirDb.equals(dir);
    }

    @Test
    void saveAsChild() {
        var parentPath = "test";
        var childPath = "test/portraits";

        Directory parent = Directory.builder()
                .path(parentPath)
                .build();
        dirRepository.save(parent);

        Directory portraits = Directory.builder()
                .path(childPath)
                .build();

        // Save child and associate to parent
        var portraitsDirOpt = dirRepository.saveAsChild(parent, portraits);
        assertTrue(
                portraitsDirOpt.isPresent(),
                "Should return Optional with saved child"
        );
        assertEquals(
                portraits,
                portraitsDirOpt.get(),
                "Returned child should match input child"
        );

        // Verify child dir was saved
        Directory dbPortraitsDir = dirRepository
                .findById(portraits.getPath())
                .orElseThrow();
        assertEquals(portraits, dbPortraitsDir);
    }

    @Test
    void saveAsChildNonExistentParent() {
        var parentPath = "non/existent/parent";
        var childPath = "non/existent/parent/child";

        Directory parent = Directory.builder()
                .path(parentPath)
                .build();

        Directory child = Directory.builder()
                .path(childPath)
                .build();

        // Attempt to save child with non-existent parent
        var childOpt = dirRepository.saveAsChild(parent, child);
        assertTrue(
                childOpt.isEmpty(),
                "Should return empty Optional for non-existent parent"
        );
        assertEquals(
                0,
                dirRepository.count(),
                "No directories should have been saved"
        );
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
        var countBeforeUpdate = dirRepository.count();
        dir.setRecursive(false);
        dirRepository.save(dir);

        // Assert no new dir node was created
        var countAfterUpdate = dirRepository.count();
        assert countAfterUpdate == countBeforeUpdate;

        // Assert dir node was updated in database
        var dbDir = dirRepository.findById(path).orElseThrow();
        assertEquals(dir, dbDir);
    }

    @Test
    void updateByParent() {
        Directory parent = createDir("test/parent");
        createDir(parent, "test/parent/dir1");
        createDir(parent, "test/parent/dir2");
        createDir(parent, "test/parent/dir3", DirStatus.VERIFYING);

        // Update children to 'VERIFYING' status
        long updatedCount = dirRepository
                .updateStatusByParent(parent, DirStatus.VERIFYING);

        // Only two should be updated since the third one is already on 'VERIFYING'
        assertEquals(2, updatedCount, "Updated count should be 2");
    }

    @Test
    void findByParentAndStatus() {
        Directory parent = createDir("test/parent");
        Directory dir1 = createDir(parent, "test/parent/dir1", DirStatus.VERIFYING);
        Directory dir2 = createDir(parent, "test/parent/dir2", DirStatus.VERIFYING);
        Directory dir3 = createDir(parent, "test/parent/dir3", DirStatus.SCAN_PENDING);

        Set<Directory> dirsInVerifying = dirRepository
                .findByParentPathAndStatus(parent, DirStatus.VERIFYING);

        assertEquals(2, dirsInVerifying.size());
        assertTrue(dirsInVerifying.contains(dir1));
        assertTrue(dirsInVerifying.contains(dir2));
        assertFalse(dirsInVerifying.contains(dir3));
    }

    @Test
    void findByParentAndStatusEmptyResults() {

        // Create dirs without parent->child association
        Directory parent = createDir("test/parent");
        createDir("test/parent/dir1", DirStatus.VERIFYING);
        createDir("test/parent/dir2", DirStatus.VERIFYING);

        Set<Directory> dirsInVerifying = dirRepository
                .findByParentPathAndStatus(parent, DirStatus.VERIFYING);
        assertTrue(dirsInVerifying.isEmpty(), "Should return empty set");
    }

    @Test
    void findByParentAndStatusNonExistentParent() {
        createDir("test/parent_dir/dir1", DirStatus.VERIFYING);
        createDir("test/parent_dir/dir2", DirStatus.VERIFYING);

        // Parent is never saved
        Directory parent = Directory.builder()
                .path("test/parent_dir")
                .build();

        Set<Directory> dirsInVerifying = dirRepository
                .findByParentPathAndStatus(parent, DirStatus.VERIFYING);
        assertTrue(dirsInVerifying.isEmpty(), "Should return empty set");
    }

    @Test
    void deleteWithDescendants() {
        Directory root = createDir("root/");
        Directory dir1 = createDir(root, "root/dir1");
        Directory dir2 = createDir(root, "root/dir2");
        Directory dir3 = createDir(dir1, "root/dir3");
        createDir(dir2, "root/dir4");
        createDir(dir3, "root/dir5");

        long deleteCount = dirRepository.deleteWithDescendants(dir2);
        assertEquals(2, deleteCount, "Deleted count should be 2");

        deleteCount = dirRepository.deleteWithDescendants(root);
        assertEquals(4, deleteCount, "Deleted count should be 6");
    }

    private Directory createDir(String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();

        return createDir(null, dir);
    }

    private Directory createDir(Directory parent, String path) {
        Directory dir = Directory.builder()
                .path(path)
                .build();

        return createDir(parent, dir);
    }

    @SuppressWarnings("UnusedReturnValue")
    private Directory createDir(
            String path,
            @SuppressWarnings("SameParameterValue") DirStatus status
    ) {
        Directory dir = Directory.builder()
                .path(path)
                .status(status)
                .build();

        return createDir(null, dir);
    }

    private Directory createDir(Directory parent, String path, DirStatus dirStatus) {
        Directory dir = Directory.builder()
                .path(path)
                .status(dirStatus)
                .build();

        return createDir(parent, dir);
    }

    private Directory createDir(Directory parent, Directory dir) {
        if (parent != null) {
            dirRepository.saveAsChild(parent, dir);
        } else {
            dirRepository.save(dir);
        }

        return dir;
    }
}
