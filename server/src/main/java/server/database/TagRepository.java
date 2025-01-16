package server.database;

import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * This method finds the Tag by its name
     * @param name - the name of the Tag
     * @return the Tag found by the name
     */
    Tag findByName(String name);
}
