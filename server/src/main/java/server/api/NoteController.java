package server.api;
import commons.Collection;
import commons.Note;
import commons.User;
import jakarta.ws.rs.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.database.ServerRepository;
import server.database.UserRepository;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;

    /**
     * Constructor with repositories for database injection
     * @param noteRepository - NoteRepository
     * @param collectionRepository - CollectionRepository
     * @param serverRepository - ServerRepository
     * @param userRepository - UserRepository
     */
    public NoteController(NoteRepository noteRepository, CollectionRepository collectionRepository
            , ServerRepository serverRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint for creating notes
     * @param note - Note object
     * @return - Response, indicating note was created
     */
    @PostMapping("/create")
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        User user = new User("Default");
        userRepository.save(user); // User entity non-existent in the database

        // Retrieve the existing collection from the database by its ID
        //int collectionId = note.getCollection().getCollectionId();
        // 1 is an example collection in the database
        Collection existingCollection = collectionRepository.findById((long)1)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
        User existingUser = userRepository.findById((long)1)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Set the existing collection in the note (ensure relationship consistency)
        note.setCollection(existingCollection);
       note.setUser(existingUser);

        // Save the note
        Note savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }
    /**
     * Endpoint for removing notes
     * @param id - Long containing id of the note to be removed
     * @return - Response, indicating note was removed
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (!noteRepository.existsById(id)) {
            return ResponseEntity.notFound().build(); // Return Not Found if the note does not exist
        }
        noteRepository.deleteById(id); // Delete the note from the database
        return ResponseEntity.noContent().build(); // Return no content
    }

    /**
     * Endpoint for fetching all notes
     * @return List<Note> containing all notes from the databasee
     */
    @GetMapping("/fetch")
    public List<Note> getAllNotes() {
        return noteRepository.findAll(); // Fetches all notes from the database
    }

    /**
     * Endpoint for fetching a note by ID
     * @param id - Long containing id of the note
     * @return The note with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id) {
        return noteRepository.findById(id)
                .map(note -> ResponseEntity.ok(note)) // Maps to found entity
                .orElseGet(() -> ResponseEntity.notFound().build()); // Else returns not found
    }

}



