package server.database;

import commons.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    //empty

    /**
     * This method finds the note titles by the collection id
     * @param collectionId - the id of the collection
     * @return a list of note titles
     */
    @Query("SELECT n.title FROM Note n WHERE n.collection.collectionId = :collectionId")
    List<String> findNoteTitlesByCollectionId(@Param("collectionId") Long collectionId);
}
