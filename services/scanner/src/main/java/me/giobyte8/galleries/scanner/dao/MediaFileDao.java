package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.MediaFile;
import me.giobyte8.galleries.scanner.model.MediaFileStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface MediaFileDao extends CrudRepository<MediaFile, String> {

    long countByStatus(MediaFileStatus status);

    @Query(
            value = "SELECT mf.* " +
                    "  FROM media_file mf " +
                    " INNER JOIN dir_contains_mfile dcm " +
                    "       ON mf.hashed_path = dcm.file_hashed_path " +
                    " WHERE dcm.dir_hashed_path = :dirHashedPath",
            nativeQuery = true
    )
    Stream<MediaFile> findByDir(String dirHashedPath);

    @Query(
            value = "SELECT mf.* " +
                    "  FROM media_file mf " +
                    " INNER JOIN dir_contains_mfile dcm " +
                    "       ON mf.hashed_path = dcm.file_hashed_path " +
                    " WHERE dcm.dir_hashed_path = :dirHashedPath " +
                    "   AND mf.status <> :mfStatus ",
            nativeQuery = true
    )
    Stream<MediaFile> findByDirAndStatusNot(
            String dirHashedPath,
            String mfStatus
    );

    @Query(
            value = "SELECT mf.* " +
                    "  FROM media_file mf " +
                    " WHERE mf.status = :mfStatus " +
                    "   AND NOT EXISTS(" +
                    "       SELECT 1 FROM dir_contains_mfile dcm " +
                    "        WHERE dcm.file_hashed_path = mf.hashed_path " +
                    ")",
            nativeQuery = true
    )
    Stream<MediaFile> findAllByStatusAndMediaDirsEmpty(String mfStatus);

    @Modifying
    @Query(
        value = "DELETE FROM media_file mf " +
                " WHERE mf.status = :mfStatus " +
                "  AND 0 = (" +
                "      SELECT COUNT(*)" +
                "        FROM dir_contains_mfile hasMFile " +
                "       WHERE hasMFile.file_hashed_path = mf.hashed_path" +
                "  )",
        nativeQuery = true)
    int deleteByStatusAndMediaDirsEmpty(String mfStatus);

    @Modifying
    @Query(" UPDATE MediaFile mf " +
            "   SET mf.status = :mfStatus " +
            " WHERE mf.status <> :mfStatus" +
            "   AND mf.hashedPath IN (" +
            "       SELECT dirHasMFile.fileHashedPath " +
            "         FROM ContentDirHasMFile dirHasMFile " +
            "        WHERE dirHasMFile.dirHashedPath = :hashedDPath" +
            ")")
    int updateStatusByMediaDir(String hashedDPath, MediaFileStatus mfStatus);

    @Query(
            value = "SELECT mf.* " +
                    "  FROM media_file mf " +
                    " INNER JOIN dir_contains_mfile dcm " +
                    "       ON mf.hashed_path = dcm.file_hashed_path " +
                    " WHERE dcm.dir_hashed_path = :hashedDPath " +
                    "   AND mf.status = :mfStatus ",
            nativeQuery = true
    )
    Stream<MediaFile> findByDirAndMediaFileStatus(
            String hashedDPath,
            String mfStatus
    );
}
