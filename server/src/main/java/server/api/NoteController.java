package server.api;
import commons.Collection;
import commons.Note;
import commons.Server;
import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.database.ServerRepository;
import server.database.UserRepository;

import java.util.List;
import java.util.MissingResourceException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteRepository noteRepository;
    private final CollectionRepository collectionRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, CollectionRepository collectionRepository, ServerRepository serverRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/")
    public ResponseEntity<Note> createNote(@RequestBody Note note) {


        // Retrieve the existing collection from the database by its ID
        //int collectionId = note.getCollection().getCollectionId();  // Assuming the collection ID is passed in the request
        Collection existingCollection = collectionRepository.findById((long)152)
                .orElseThrow(() -> new NotFoundException("Collection not found"));

        // Set the existing collection in the note (ensure relationship consistency)
        note.setCollection(existingCollection);


        // Save the note
        Note savedNote = noteRepository.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNote);
    }
}



