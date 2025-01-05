package server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import server.database.CollectionRepository;
import server.database.NoteRepository;
import server.database.ServerRepository;
import commons.Collection;
import commons.Note;
import commons.Server;
import server.database.TagRepository;

import static org.mockito.Mockito.when;

@WebMvcTest(NoteController.class)
public class NoteControllerTest {
    //Class that simulates HTTP requests, executes them, and verifies the responses
    //All without needing a real database
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private CollectionRepository collectionRepository;

    @MockBean
    private ServerRepository serverRepository;

    @MockBean
    private TagRepository tagRepository;

    private Note note;
    private Collection collection;
    private Server server;

    @BeforeEach
    public void setup() {
        //Setup mock data to be used
        server = new Server();
        server.setServerId(0);

        collection = new Collection();
        collection.setCollectionId(0);
        collection.setCollectionTitle("CollectionForTest");
        collection.setServer(server);

        note = new Note();
        note.setNoteId(1L);
        note.setTitle("Test Note");
        note.setBody("Test Note");
        note.setCollection(collection);
    }

    /**
     * Tests the createNote endpoint, mocking the database behaviour
     * @throws Exception
     */
    @Test
    public void testCreateNote() throws Exception {
        //Mock the database operations (simulate)
        when(serverRepository.findById(0L)).thenReturn(java.util.Optional.of(server));
        when(collectionRepository.findById(0L)).thenReturn(java.util.Optional.of(collection));
        when(noteRepository.save(Mockito.any(Note.class))).thenReturn(note);
        when(tagRepository.findByName(Mockito.anyString())).thenReturn(null);

        //Create a JSON string for the note
        var json = new ObjectMapper().writeValueAsString(note);

        //Perform the request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/notes/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Note"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value("Test Note"));
    }
}
