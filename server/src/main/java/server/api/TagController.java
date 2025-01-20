package server.api;

import commons.Note;
import commons.Tag;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.NoteRepository;
import server.database.TagRepository;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructs a new TagController with the specified repositories.
     *
     * @param tagRepository The repository for managing tag entities.
     * @param noteRepository The repository for managing note entities.
     */
    public TagController(TagRepository tagRepository, NoteRepository noteRepository) {
        this.tagRepository = tagRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Renames an existing tag in the database and updates all notes associated with the tag.
     *
     * @param id The ID of the tag to rename.
     * @param newName The new name for the tag.
     * @return A ResponseEntity containing the updated tag if successful.
     * @throws RuntimeException If the tag with the given ID is not found.
     */
    @Transactional
    @PutMapping("/rename/{id}")
    public ResponseEntity<Tag> renameTag(@PathVariable Long id, @RequestBody String newName) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(newName);
        tagRepository.save(tag);

        List<Note> affectedNotes = noteRepository.findAll().stream()
                .filter(note -> note.getTags().contains(tag))
                .toList();

        for (Note note : affectedNotes) {
            noteRepository.save(note); // Trigger note update
        }

        return ResponseEntity.ok(tag);
    }


    /**
     * Deletes a tag from the database and removes its association with all related notes.
     *
     * @param id The ID of the tag to delete.
     * @return A ResponseEntity with no content if the deletion is successful, or an error message
     *         if the tag is not found.
     */
    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tag not found");
        }

        // Remove the tag from associated notes
        Tag tag = tagRepository.findById(id).orElseThrow();
        List<Note> notesWithTag = noteRepository.findAll().stream()
                .filter(note -> note.getTags().contains(tag))
                .toList();

        for (Note note : notesWithTag) {
            note.getTags().remove(tag);
            noteRepository.save(note);
        }

        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }



    /**
     * Endpoint to fetch all tags from the database.
     * @return List of tags.
     */
    @GetMapping("/fetch")
    public List<Note> getAllNotes() {
        List<Note> notes = noteRepository.findAll();
        notes.forEach(note -> System.out.println("Note: "
                + note.getTitle() + " Tags: " + note.getTags()));
        return notes; // Spring automatically serializes this into JSON
    }

}
