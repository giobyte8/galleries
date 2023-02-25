package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDirHasMFile;
import me.giobyte8.galleries.scanner.model.DirHasMFileId;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentDirHasMFileDao
        extends JpaRepository<ContentDirHasMFile, DirHasMFileId> {

    @Modifying
    @Query("DELETE FROM ContentDirHasMFile hasMFile " +
            " WHERE hasMFile.dirHashedPath = :dirHashedPath " +
            "   AND hasMFile.fileHashedPath IN (" +
            "       SELECT mFile.hashedPath " +
            "         FROM MediaFile mFile " +
            "        WHERE mFile.status = :mfStatus" +
            "   )")
    int deleteAllByDirHashedPathAndMFileStatus(
            String dirHashedPath,
            MediaFileStatus mfStatus
    );
}
