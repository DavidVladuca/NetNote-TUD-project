package server.api;

import commons.Note;
import commons.Tag;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.NoteRepository;
import server.database.TagRepository;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;

    public TagController(TagRepository tagRepository, NoteRepository noteRepository) {
        this.tagRepository = tagRepository;
        this.noteRepository = noteRepository;
    }

    // Rename Tag
    @Transactional
    @PutMapping("/rename/{id}")
    public ResponseEntity<Void> renameTag(@PathVariable Long id, @RequestBody String newName) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(newName);
        tagRepository.save(tag);
        return ResponseEntity.ok().build();
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
