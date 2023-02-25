package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface MediaFileDao extends JpaRepository<MediaFile, String> {

    @Modifying
    @Query("DELETE FROM MediaFile mf " +
            "WHERE mf.status = :mfStatus " +
            "  AND mf.mediaDirs IS EMPTY")
    int deleteByStatusAndMediaDirsEmpty(MediaFileStatus mfStatus);

    Stream<MediaFile> findAllByStatusAndMediaDirsEmpty(MediaFileStatus mfStatus);

//    @Query("SELECT MediaFile FROM MediaFile mf " +
//            " LEFT JOIN ContentDirHasMFile hasMFile " +
//            "   ON hasMFile.fileHashedPath = mf.hashedPath " +
//            "WHERE hasMFile.dirHashedPath = :hashedDPath" +
//            "  AND mf.status = :mfStatus ")
    Stream<MediaFile> findAllByMediaDirsHashedPathAndStatus(
            String hashedDPath,
            MediaFileStatus mfStatus
    );

    long countByStatus(MediaFileStatus status);
}
