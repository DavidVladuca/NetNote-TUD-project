package server;

import commons.Collection;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class CollectionController {
    private CollectionRepository collectionRepository;

    public CollectionController(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @GetMapping("/{title}")
    public Collection getCollectionByTitle(@PathVariable String title) {
        Collection collection = collectionRepository.findByCollectionTitle(title);
        if (collection == null) {
            throw new RuntimeException("Collection not found");
        }
        return collection;
    }

    @PostMapping("/")
    public String createCollection(@RequestBody Collection newCollection) {
        collectionRepository.save(newCollection);
        return "Collection created successfully!";
    }

    @GetMapping("/")
    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

}
