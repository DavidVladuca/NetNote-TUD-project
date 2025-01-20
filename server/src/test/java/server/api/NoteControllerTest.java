package server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Tag;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
     *
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

    /**
     * Tests the update note endpoint, mocking the database behaviour
     *
     * @throws Exception
     */
    @Test
    public void testUpdateNoteSuccess() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Note updated = new Note();
        updated.setNoteId(1L);
        updated.setTitle("Test Note updated");
        updated.setBody("Test Note updated");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteRepository.save(updated)).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.noteId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Note updated"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value("Test Note updated"));
    }

    /**
     * Tests the update note endpoint, when the ID is less than 1
     *
     * @throws Exception
     */
    @Test
    public void testUpdateNoteBadId() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Note badId = new Note();
        badId.setNoteId(0L);
        badId.setTitle("Test Note");
        badId.setBody("Test Note");

        Note updated = new Note();
        updated.setNoteId(0L);
        updated.setTitle("Test Note");
        updated.setBody("Test Note");

        when(noteRepository.findById(0L)).thenReturn(Optional.of(badId));
        when(noteRepository.save(updated)).thenReturn(updated);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    /**
     * Tests the delete endpoint, expect no content after deletion
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNoteSuccess() throws Exception {
        when(noteRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/notes/delete/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    /**
     * Tests the delete endpoint with bad id, expect 404 not found
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNoteBadId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/notes/delete/{id}", 0L))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    /**
     * Tests the get endpoint with a valid id, expect 200 Ok
     *
     * @throws Exception
     */
    @Test
    public void testGetNoteByIdSuccess() throws Exception {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.noteId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Note"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value("Test Note"));
    }
/*

     * Tests the getter for the tags of a note by its ID
     *
     * @throws Exception

    @Test
    public void testGetTagsByNoteId() throws Exception {
        Note tagNote = new Note();
        tagNote.setNoteId(20L);
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        Set<Tag> tags = new HashSet<>(Set.of(tag1, tag2));
        note.setTags(tags);

        when(noteRepository.findById(20L)).thenReturn(Optional.of(note));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/{id}/tags", 20L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2)) // Check number of tags
                .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.name == 'Tag1')]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.name == 'Tag2')]").exists());
    }
*/
    @Test
    public void testValidateTitleDuplicate() throws Exception {
        when(noteRepository.existsByCollectionCollectionIdAndTitle(0L, "Test Note")).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/validate-title")
                        .param("collectionId", "0")
                        .param("title", "Test Note"))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void testValidateTitleDifferent() throws Exception {
        when(noteRepository.existsByCollectionCollectionIdAndTitle(0L, "Test Note")).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/validate-title")
                        .param("collectionId", "0")
                        .param("title", "Test Note 2"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}