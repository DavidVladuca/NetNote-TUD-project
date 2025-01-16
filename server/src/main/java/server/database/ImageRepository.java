package server.database;

import commons.Images;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Images, Long> {
    /**
     * This method finds all images by note id
     * @param noteId - the note id provided
     * @return a list of Images based on the id
     */
    @SuppressWarnings("checkstyle:MethodName")
    List<Images> findAllByNote_NoteId(Long noteId);
    //empty
}
