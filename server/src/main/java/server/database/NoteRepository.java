package server.database;

import commons.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    /**
     * Checks if a note with the given title exists within the specified collection.
     * @param collectionId - The ID of the collection
     * @param title - The title of the note to be checked
     * @return - True if a note with the title exists in the collection, else returns false
     */
    boolean existsByCollectionCollectionIdAndTitle(Long collectionId, String title);
}
