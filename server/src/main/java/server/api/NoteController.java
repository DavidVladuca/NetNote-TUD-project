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
     * This method ensure the update of the Note content using the 'put' json mapping
     * @param note - note that was provided with json
     * @return a response entity with the status of the execution
     */
    @PutMapping("/update")
    public ResponseEntity<Note> updateNote(@RequestBody Note note) {
        // todo - Server and collection should not be manually added here
        //  but serialized and deserialized in json automatically
        Server server = serverRepository.findById(0L)
                .orElseGet(() -> {
                    Server newServer = new Server();
                    newServer.setServerId(0);
                    return serverRepository.save(newServer);
                });

        Collection collection = collectionRepository.findById(0L).orElseGet(() -> {
            Collection defaultCollection = new Collection();
            defaultCollection.setCollectionId(0);
            defaultCollection.setCollectionTitle("Default Collection");
            defaultCollection.setServer(server);
            return collectionRepository.save(defaultCollection);
        });

        // Set the existing collection in the note (ensure relationship consistency)
        note.setCollection(collection);

        System.out.println("Received update request for note: " + note);

        if (note.getNoteId() <= 0) {
            System.err.println("Invalid note ID: " + note.getNoteId());
            return ResponseEntity.badRequest().build();
        }

        // Check if the note exists in the database
        Note existingNote = noteRepository.findById(note.getNoteId())
                .orElseThrow(() -> new IllegalArgumentException("Note not found for ID: " + note.getNoteId()));

        // Handle null or missing collection
        if (note.getCollection() == null) {
            note.setCollection(existingNote.getCollection()); // Use the existing collection if not provided
        }

        try {
            // Save the updated note
            Note updatedNote = noteRepository.save(note);
            return ResponseEntity.ok(updatedNote);
        } catch (Exception e) {
            System.err.println("Error updating note: " + e.getMessage());
            e.printStackTrace();
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



