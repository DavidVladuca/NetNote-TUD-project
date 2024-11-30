package server;

import commons.Collection;
import commons.Note;
import commons.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.database.UserRepository;

@Controller
@RequestMapping("/")
public class NoteController {

    @GetMapping("/")
    @ResponseBody
    public String index() {return "Hello world!";}

    private NoteRepository noteRepository;
    private CollectionRepository collectionRepository;
    private UserRepository userRepository;

    public NoteController(NoteRepository noteRepository, CollectionRepository collectionRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/note/{title}/{body}/{collection}/{user}")
    @ResponseBody
    public String note(@PathVariable String title,
                       @PathVariable String body,
                       @PathVariable String collection,
                       @PathVariable String user) {
        Collection collectionEntity = collectionRepository.findByCollectionTitle(collection);
        if (collectionEntity == null) {
            return "Collection not found";
        }

        User userEntity = userRepository.findById(Long.parseLong(user));
        if (userEntity == null) {
            return "User not found";
        }

        var n = new Note(title, body, collectionEntity, userEntity);
        n.title = title;
        noteRepository.save(n);

        return "Note saved";
    }
}


