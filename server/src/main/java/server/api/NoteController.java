package server.api;
import commons.Collection;
import commons.Note;
import commons.Server;
import commons.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.*;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final TagRepository tagRepository;
    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;
    private final ServerRepository serverRepository;

    /**
     * Constructor with repositories for database injection
     * @param noteRepository - NoteRepository
     * @param collectionRepository - CollectionRepository
     * @param serverRepository - ServerRepository
     * @param tagRepository - TagRepository
     */
    public NoteController(NoteRepository noteRepository, CollectionRepository collectionRepository
            , ServerRepository serverRepository, TagRepository tagRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.serverRepository = serverRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Endpoint for creating notes - still working with only 1 Default Collection
     * @param note - Note object
     * @return - Response, indicating note was created
     */
    @PostMapping("/create")
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        // 0 is the default collection in the database
        // Ensure default Server exists - if not, create it
        Server server = serverRepository.findById(0L)
                .orElseGet(() -> {
                    Server newServer = new Server();
                    newServer.setServerId(0);
                    return serverRepository.save(newServer);
                });

        // Ensure default Collection exists - if not, again, create it
        Collection collection = collectionRepository.findById(0L).orElseGet(() -> {
            Collection defaultCollection = new Collection();
            defaultCollection.setCollectionId(0);
            defaultCollection.setCollectionTitle("Default Collection");
            defaultCollection.setServer(server);
            return collectionRepository.save(defaultCollection);
        });
        // Set the existing collection in the note (ensure relationship consistency)
        note.setCollection(collection);

        // Handle tags
        handleTags(note);

        // Save the note
        Note savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }

    /**
     * This method ensure the update of the Note content using the 'put' json mapping
     * @param note - note that was provided with json
     * @return a response entity with the status of the execution
     */
    @PutMapping("/update")
    public ResponseEntity<Note> updateNote(@RequestBody Note note) {
        if (note.getNoteId() <= 0) {
            System.err.println("Invalid note ID: " + note.getNoteId());
            return ResponseEntity.badRequest().build();
        }

        // Check if the note exists in the database
        Note existingNote = noteRepository.findById(note.getNoteId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found for ID: " + note.getNoteId()));

        // Use the existing collection if not provided in the request
        if (note.getCollection() == null) {
            note.setCollection(existingNote.getCollection());
        }

        // Check for title change
        String oldTitle = existingNote.getTitle();
        String newTitle = note.getTitle();

        // Handle tags
        handleTags(note);

        try {
            // Save the updated note with new tags
            Note updatedNote = noteRepository.save(note);

            // If the title has changed, update references in other notes
            if (!oldTitle.equals(newTitle)) {
                updateReferences(oldTitle, newTitle);
            }

            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update all the references from notes when the title of the referenced
     * note was changed
     * @param oldTitle - the original title
     * @param newTitle - the new title
     */
    @PutMapping("/updateRefs")
    private void updateReferences(String oldTitle, String newTitle) {
        System.out.println("Starting reference update: [" + oldTitle + "] -> [" + newTitle + "]");
        List<Note> allNotes = noteRepository.findAll();

        for (Note note : allNotes) {
            String body = note.getBody();

            // Log the body before changes
            System.out.println("Processing note: " + note.getTitle());
            System.out.println("Original body: " + body);

            // Update references
            String updatedBody = body.replaceAll("\\[\\[" + Pattern.quote(oldTitle) + "\\]\\]", "[[" + newTitle + "]]");

            if (!updatedBody.equals(body)) {
                System.out.println("Updated body for note: " + note.getTitle());
                System.out.println("Updated body: " + updatedBody);
                note.setBody(updatedBody);

                // Save the updated note
                noteRepository.save(note);
            } else {
                System.out.println("No references to update for note: " + note.getTitle());
            }
        }
        System.out.println("Reference update completed.");
    }




    @PutMapping("/updateTags")
    private void handleTags(@RequestBody Note note) {
        Set<Tag> managedTags = new HashSet<>();
        for (Tag tag : note.getTags()) {
            // Find existing tags or create new ones
            Tag existingTag = tagRepository.findByName(tag.getName());
            managedTags.add(Objects.requireNonNullElseGet(existingTag, () -> tagRepository.save(new Tag(tag.getName()))));
        }
        note.setTags(managedTags);
    }

    @GetMapping("/{id}/tagsGetter")
    public ResponseEntity<Set<Tag>> getTagsForNote(@PathVariable Long id) {
        return noteRepository.findById(id)
                .map(note -> ResponseEntity.ok(note.getTags()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/tagsUpdate")
    public ResponseEntity<Note> updateTagsForNote(@PathVariable Long id, @RequestBody Set<String> tagNames) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found for ID: " + id));

        Set<Tag> managedTags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag existingTag = tagRepository.findByName(tagName);
            if (existingTag == null) {
                // Create and save the new tag if it doesn't exist
                existingTag = tagRepository.save(new Tag(tagName));
            }
            managedTags.add(existingTag);
        }

        // Update the note's tags
        note.setTags(managedTags);

        try {
            Note updatedNote = noteRepository.save(note);
            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            System.err.println("Error updating tags: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Endpoint for removing notes
     * @param id - Long containing id of the note to be removed
     * @return - Response, indicating note was removed
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (!noteRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // Return Not Found if the note does not exist
        }
        noteRepository.deleteById(id); // Delete the note from the database
        return ResponseEntity.noContent().build(); // Return no content
    }

    /**
     * Endpoint for fetching all notes
     * @return List<Note> containing all notes from the database
     */
    @GetMapping("/fetch")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }



    /**
     * Endpoint for fetching a note by ID
     * @param id - Long containing id of the note
     * @return The note with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return noteRepository.findById(id)
                .map(ResponseEntity::ok) // Maps to found entity
                .orElseGet(() -> ResponseEntity.notFound().build()); // Else returns not found
    }

    /**
     * Validates if a title already exists within the given collection.
     * @param title - Title to check for duplicates
     * @return - HTTP 409 Conflict if duplicate, 200 OK otherwise
     */
    @GetMapping("/validate-title")
    public ResponseEntity<Void> validateTitle(@RequestParam String title) {
        // Checks if there is the same title - even when there is a space at the end, that is ensured by trim()
        boolean isDuplicate = noteRepository.existsByCollectionCollectionIdAndTitle(0L, title.trim());
        if (isDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // returns 409 Conflict Code
        }
        return ResponseEntity.ok().build(); // Returns 200 OK Code
    }
}



