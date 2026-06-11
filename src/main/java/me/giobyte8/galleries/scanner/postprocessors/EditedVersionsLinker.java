package me.giobyte8.galleries.scanner.postprocessors;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EditedVersionsLinker implements DirScanPostProcessor {

    private static final int PAGE_SIZE = 200;

    // Matches file names like "photo_edit.heic" or "photo_EDIT.jpg".
    // Captures the original base name and preserves the extension.
    private static final Pattern EDITED_VERSION_FILE_NAME_PATTERN = Pattern.compile(
            "^(?<base>.+)_edit(?<ext>\\.[^.]+)$",
            Pattern.CASE_INSENSITIVE
    );

    private final ImageRepository imgRepository;

    @Override
    public void process(Directory dir) {
        String afterPath = null;

        while (true) {
            List<String> paths = imgRepository.findUnlinkedEditedPathsByParent(
                    dir,
                    afterPath,
                    PAGE_SIZE
            );
            if (paths.isEmpty()) {
                return;
            }

            for (String editedPath : paths) {
                var originalPathOpt = toOriginalPath(editedPath);
                if (originalPathOpt.isEmpty()) {
                    continue;
                }

                // If the original image is not present in this directory yet,
                // no relationship is created (Path will be retried on future scans)
                imgRepository.linkEditedToOriginal(
                        dir,
                        editedPath,
                        originalPathOpt.orElseThrow()
                );
            }

            afterPath = paths.getLast();
        }
    }

    private Optional<String> toOriginalPath(String editedPath) {

        // Parse editedPath and extract only the file-name portion for suffix
        // matching. If there is no file-name segment, this path is invalid.
        Path path = Path.of(editedPath);
        Path fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return Optional.empty();
        }

        // Accept only names in the form "<base>_edit.<ext>" where matching
        // is case-insensitive (_edit, _EDIT, ...).
        String fileName = fileNamePath.toString();
        var matcher = EDITED_VERSION_FILE_NAME_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        // Build the original filename by removing only the trailing "_edit"
        // token while preserving the original extension.
        String originalBase = matcher.group("base");
        if (originalBase.isBlank()) {
            return Optional.empty();
        }

        // Keep the original file in the same directory as editedPath.
        String originalFileName = originalBase + matcher.group("ext");
        Path parent = path.getParent();
        Path originalPath = parent == null
                ? Path.of(originalFileName)
                : parent.resolve(originalFileName);

        return Optional.of(originalPath.toString());
    }
}
