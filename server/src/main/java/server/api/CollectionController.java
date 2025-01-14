package server.api;

import commons.Collection;
import commons.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.database.ServerRepository;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionRepository collectionRepository;
    private final ServerRepository serverRepository;

    /**
     * Constructor with repositories for database injection
     * @param collectionRepository - CollectionRepository
     * @param serverRepository - ServerRepository
     */
    public CollectionController(CollectionRepository collectionRepository, ServerRepository serverRepository) {
        this.collectionRepository = collectionRepository;
        this.serverRepository = serverRepository;
    }

    /**
     * Endpoint for creating collections
     * @param collection - Collection object
     * @return - ResponseEntity with the saved collection
     */
    @PostMapping("/create")
    public ResponseEntity<Collection> createCollection(@RequestBody Collection collection) {
        // Ensure a default server exists for the collection
        Server server = collection.getServer();
        if (server == null || serverRepository.findById(server.getServerId()).isEmpty()) {
            server = serverRepository.findById(0L)
                    .orElseGet(() -> {
                        Server defaultServer = new Server();
                        defaultServer.setServerId(0);
                        return serverRepository.save(defaultServer);
                    });
            collection.setServer(server);
        }

        // Save the collection
        Collection savedCollection = collectionRepository.save(collection);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCollection);
    }

    /**
     * Endpoint for updating a collection
     * @param collection - Collection object to be updated
     * @param id - ID of the collection that needs an update
     * @return - ResponseEntity with the updated collection
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Collection> updateCollection(@PathVariable Long id, @RequestBody Collection collection) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // Find the existing collection
        Collection existingCollection = collectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found for ID: " + id));

        // Update the collection details
        existingCollection.setCollectionTitle(collection.getCollectionTitle());
        existingCollection.setServer(collection.getServer() != null ? collection.getServer() : existingCollection.getServer());

        // Save the updated collection
        Collection updatedCollection = collectionRepository.save(existingCollection);
        return ResponseEntity.ok(updatedCollection);
    }

    /**
     * Endpoint for fetching all collections
     * @return - List of collections
     */
    @GetMapping("/fetch")
    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

    /**
     * Endpoint for deleting a collection by ID
     * @param id - ID of the collection to delete
     * @return - ResponseEntity indicating success or failure
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build(); // Invalid ID
        }

        // Check if the collection exists
        if (!collectionRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // Collection not found
        }

        // Delete the collection
        collectionRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // Return 204 No Content on successful deletion
    }
}
