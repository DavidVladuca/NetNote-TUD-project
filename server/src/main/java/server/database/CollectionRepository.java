package server.database;

import commons.Collection;
import commons.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Integer> {
    Collection findByCollectionTitle(String collectionTitle);
    List<Collection> findByServer(Server server); //to be implemented
    Collection findById(int id); //to be implemented
}
