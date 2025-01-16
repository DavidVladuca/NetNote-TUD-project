package server.api;

import commons.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.TagRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;

    /**
     * This method creates the tag via the /create endpoint
     * @param tag - the tag to be created
     * @return a response entity with the tag
     */
    @PostMapping("/create")
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        if (tag == null || tag.getName() == null || tag.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        tagRepository.save(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    /**
     * This method is just for testing if the endpoint is working
     * @return a String sasying the TagController is working
     */
    @GetMapping("/test")
    public String testEndpoint() {
        return "TagController is working!";
    }

    /**
     * the constructor for the TagController
     * @param tagRepository - the repository of the tags
     */
    @Autowired
    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
        System.out.println("TagController initialized");
    }

    /**
     * the getter for all the tags
     * @return - all tags in the repository
     */
    @GetMapping
    public List<Tag> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        // Ensure notes are fetched for each tag to avoid lazy-loading issues
        for (Tag tag : tags) {
            tag.getNotes().size(); // Trigger loading of notes
        }
        return tags;
    }
}

