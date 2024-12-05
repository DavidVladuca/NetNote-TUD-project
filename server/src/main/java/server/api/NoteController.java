package server.api;
import commons.Collection;
import commons.Note;
import commons.Server;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.database.ServerRepository;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;
    private final ServerRepository serverRepository;

    /**
     * Constructor with repositories for database injection
     * @param noteRepository - NoteRepository
     * @param collectionRepository - CollectionRepository
     * @param serverRepository - ServerRepository
     */
    public NoteController(NoteRepository noteRepository, CollectionRepository collectionRepository
            , ServerRepository serverRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.serverRepository = serverRepository;
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
                .map(note -> ResponseEntity.ok(note)) // Maps to found entity
                .orElseGet(() -> ResponseEntity.notFound().build()); // Else returns not found
    }

}



