package me.giobyte8.galleries.scanner.postprocessors;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EditedVersionsLinkerTest extends BaseIntegrationTest {

    @Autowired
    private EditedVersionsLinker linker;

    @Autowired
    private DirectoryRepository dirRepository;

    @Autowired
    private ImageRepository imgRepository;

    @Autowired
    private Driver neo4jDriver;

    @Test
    void processLinksEditedVariantsOnlyForDirectChildren() {
        // Parent directory and nested child to verify direct-child-only scope.
        Directory root = Directory.builder()
                .path("gallery/")
                .build();
        dirRepository.save(root);

        Directory child = Directory.builder()
                .path("gallery/child/")
                .build();
        dirRepository.saveAsChild(root, child);

        createImage(root, "gallery/file1.heic");
        createImage(root, "gallery/file1_edit.heic");

        createImage(root, "gallery/file2.jpg");
        createImage(root, "gallery/file2_EDIT.jpg");

        // Edited image without a corresponding original.
        createImage(root, "gallery/missing_edit.heic");

        createImage(child, "gallery/child/file3.heic");
        createImage(child, "gallery/child/file3_edit.heic");

        // Process only root; child directory is expected to be handled later
        // by its own post-processing invocation.
        linker.process(root);

        // Lowercase _edit should link to its same-directory original.
        assertThat(countEditsRelationship(
                "gallery/file1_edit.heic",
                "gallery/file1.heic"
        )).isEqualTo(1);

        // Uppercase _EDIT should also link due to case-insensitive matching.
        assertThat(countEditsRelationship(
                "gallery/file2_EDIT.jpg",
                "gallery/file2.jpg"
        )).isEqualTo(1);

        // Missing original remains unlinked in this scan execution.
        assertThat(countOutgoingEdits("gallery/missing_edit.heic")).isEqualTo(0);

        // Child edited image is not linked when processing only parent.
        assertThat(countOutgoingEdits("gallery/child/file3_edit.heic"))
                .isEqualTo(0);
    }

    private void createImage(Directory parent, String path) {
        imgRepository.saveAsChild(
                parent,
                me.giobyte8.galleries.persistence.models.Image.builder()
                        .path(path)
                        .status(MediaFileStatus.AVAILABLE)
                        .build()
        );
    }

    private long countEditsRelationship(String editedPath, String originalPath) {
        String query = """
                MATCH (edited:Image { path: $editedPath })
                    -[r:EDITS]
                    ->(original:Image { path: $originalPath })
                RETURN count(r) AS relCount""";

        try (Session session = neo4jDriver.session()) {
            return session.executeRead(ctx ->
                    ctx.run(query, Map.of(
                            "editedPath", editedPath,
                            "originalPath", originalPath
                    ))
                            .single()
                            .get("relCount")
                            .asLong()
            );
        }
    }

    private long countOutgoingEdits(String editedPath) {
        String query = """
                MATCH (:Image { path: $editedPath })-[r:EDITS]->(:Image)
                RETURN count(r) AS relCount""";

        try (Session session = neo4jDriver.session()) {
            return session.executeRead(ctx ->
                    ctx.run(query, Map.of("editedPath", editedPath))
                            .single()
                            .get("relCount")
                            .asLong()
            );
        }
    }
}





