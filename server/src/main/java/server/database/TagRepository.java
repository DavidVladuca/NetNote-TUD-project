package server.database;

import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Finds a tag by its name.
     * @param name the name of the tag to search for.
     * @return an Optional containing the Tag if found, or empty otherwise.
     */
    Optional<Tag> findByName(String name);
}
