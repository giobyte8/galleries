package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.mappers.DirRowMapper;
import me.giobyte8.galleries.persistence.mappers.ImgRowMapper;
import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

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
        var dirOpt = dirRepository.findByPath("non/existent/dir");
        assertTrue(dirOpt.isEmpty());
    }

    @Test
    void saveAndFindById() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .build();

        dirRepository.save(dir);
        var dirOpt = dirRepository.findById(dir.getId());

        assertTrue(dirOpt.isPresent());
        assertEquals(dir, dirOpt.get());
    }

    @Test
    void saveAndFindByPath() {
        final String path = "test/portraits";
        Directory dir = Directory.builder()
                .path(path)
                .build();

        dirRepository.save(dir);
        var dirOpt = dirRepository.findByPath(path);

        assertTrue(dirOpt.isPresent());
        assertEquals(dir, dirOpt.get());
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
                .findByPath(portraits.getPath())
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
        var dbDir = dirRepository.findByPath(path).orElseThrow();
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

    @Test
    void findChildren() {
        var parentPath = "root";
        Directory root = createDir(parentPath);
        for (int i = 9; i > 0; i--) {
            createDir(
                    root,
                    String.format("%s/dir%d", parentPath, i)
            );
        }

        var page = dirRepository.findChildren(
                root.getId(),
                PageRequest.of(1, 3)
        );

        assertEquals(
                9,
                page.getTotalElements(),
                "Total elements in DB should be 9"
        );
        assertEquals(
                3,
                page.getNumberOfElements(),
                "Page should contain 3 elements");
        assertEquals(
                "root/dir4",
                page.getContent().get(0).getPath(),
                "First element should be 'root/dir4'");
        assertEquals(
                "root/dir5",
                page.getContent().get(1).getPath(),
                "Second element should be 'root/dir5'");
    }

    @Test
    void findRoots() {
        createDir("root1");
        createDir("root2");
        var root3 = createDir("root3");
        createDir(root3, "root3/children1");

        var page = dirRepository.findRoots(PageRequest.of(0, 2));
        assertEquals(
                3,
                page.getTotalElements(),
                "Total elements in DB should be 3"
        );
        assertEquals(
                2,
                page.getNumberOfElements(),
                "Page should contain 2 elements");
        assertEquals(
                "root1",
                page.getContent().get(0).getPath(),
                "First element should be 'root1'");
        assertEquals(
                "root2",
                page.getContent().get(1).getPath(),
                "Second element should be 'root2'");
    }

    @Test
    void searchByPath() {
        var root = createDir("root/alpha");
        createDir("root/ALPS");
        createDir("root/beta");
        createDir(root, "root/alpha/child");

        var results = dirRepository.searchByPath("alp", 5);
        assertEquals(2, results.size(), "Should return two root matches");
        assertEquals(
                "root/ALPS",
                results.get(0).getPath(),
                "Results should be sorted by path"
        );
        assertEquals("root/alpha", results.get(1).getPath());
        assertTrue(
                results.stream().noneMatch(d -> d.getPath().equals("root/alpha/child")),
                "Child directories should be excluded"
        );

        var limited = dirRepository.searchByPath("alp", 1);
        assertEquals(1, limited.size(), "Should respect limit");
    }

    @Test
    void findWithLineageById() {
        Directory root = createDir("root");
        Directory child1 = createDir(root, "root/child1");

        var result = dirRepository.findWithLineageById(root.getId());
        assertTrue(result.isPresent(), "Result should be present");

        var dirWithLineage = result.get();
        assertEquals(root.getId(), dirWithLineage.directory().getId(), "Directory ID should match");
        assertEquals(0, dirWithLineage.lineage().size(), "Lineage should be empty for root");


        result = dirRepository.findWithLineageById(child1.getId());
        assertTrue(result.isPresent(), "Result should be present");

        dirWithLineage = result.get();
        assertEquals(
                child1.getId(),
                dirWithLineage.directory().getId(),
                "Directory ID should match");
        assertEquals(
                1,
                dirWithLineage.lineage().size(),
                "Lineage should contain one ancestor");
        assertEquals(
                root.getId(),
                dirWithLineage.lineage().getFirst().getId(),
                "Ancestor ID should match");
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
