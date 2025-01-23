package server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Tag;
import org.hamcrest.Matchers;
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
import java.util.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.mockito.Mockito.when;

@WebMvcTest(NoteController.class)
public class NoteControllerTest {
    //Class that simulates HTTP requests, executes them, and
    // verifies the responses, all without needing a real database
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
    private Note note2;

    private Note noteWithReference1;
    private Collection collection;
    private Server server;
    private Tag tag1;

    private Tag tag2;

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

        note2 = new Note();
        note2.setNoteId(2L);
        note2.setTitle("Test Note Num 2");
        note2.setBody("Other note body");
        note2.setCollection(collection);

        noteWithReference1 = new Note();
        noteWithReference1.setNoteId(3L);
        noteWithReference1.setTitle("Note w. Ref 1");
        noteWithReference1.setBody("Note with reference to [[Test Note]]");
        noteWithReference1.setCollection(collection);

        tag1 = new Tag();
        tag1.setName("Tag 1");

        tag2 = new Tag();
        tag2.setName("Tag 2");

    }

    /**
     * Tests the createNote endpoint, mocking the database behaviour.
     *
     * @throws Exception
     */
    @Test
    public void testCreateNote() throws Exception {
        //Mock the database operations (simulate)
        when(serverRepository.findById(0L))
                .thenReturn(java.util.Optional.of(server));
        when(collectionRepository.findById(0L))
                .thenReturn(java.util.Optional.of(collection));
        when(noteRepository.save(Mockito.any(Note.class))).thenReturn(note);
        when(tagRepository.findByName(Mockito.anyString())).thenReturn(null);

        //Create a JSON string for the note
        var json = new ObjectMapper().writeValueAsString(note);

        //Perform the request and verify the response
        mockMvc.perform(MockMvcRequestBuilders.post("/api/notes/create")
                        .param("collectionId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title")
                        .value("Test Note"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body")
                        .value("Test Note"));
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

    @Test
    public void testValidateTitleDuplicate() throws Exception {
        when(noteRepository.existsByCollectionCollectionIdAndTitle(
                0L,
                "Test Note"))
                .thenReturn(true);
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

    @Test
    public void testUpdateNoteInvalidId() throws Exception {
        note.setNoteId(-1L); //setting invalid ID
        var json = new ObjectMapper().writeValueAsString(note);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/notes/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }


    @Test
    public void testUpdateNoteExceptionWhileUpdating() throws Exception {
        when(noteRepository.findById(1L)).thenReturn(java.util.Optional.of(note));
        when(noteRepository.save(Mockito.any(Note.class)))
                .thenThrow(new RuntimeException("Database error"));

        var json = new ObjectMapper().writeValueAsString(note);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void testUpdateChangedName() throws Exception {
        when(noteRepository.findById(note.getNoteId())).thenReturn(Optional.of(note));
        when(noteRepository.save(Mockito.any(Note.class))).thenReturn(note);
        note.setTitle("Changed title");
        var json = new ObjectMapper().writeValueAsString(note);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title")
                        .value("Changed title"));
    }

    @Test
    void testUpdateReferencesSuccessful() throws Exception {
        when(noteRepository.findAll()).thenReturn(Arrays.asList(note, note2, noteWithReference1));
        when(noteRepository.save(Mockito.any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        note.setTitle("Some changed title");
        var json = new ObjectMapper().writeValueAsString(note);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/updateRefs")
                        .param("oldTitle", "Test Note")
                        .param("newTitle", "NewTitle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteRepository).save(argThat(any_note ->
                any_note.getBody().equals("Note with reference to [[NewTitle]]")
        ));
        verify(noteRepository, never()).save(argThat(any_note ->
                any_note.getBody().equals("Other note body")
        ));

    }

    @Test
    void testUpdateReferencesBodyUpdated() throws Exception {
        noteWithReference1.setBody("changed body");

        when(noteRepository.findAll()).thenReturn(Arrays.asList(note, note2));
        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/updateRefs")
                                .param("oldTitle", "Test Note")
                                .param("newTitle", "NewTitle")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk());
        verify(noteRepository, never()).save(argThat(some_note ->
                some_note.getBody().equals("No references to OldTitle here.")
        ));
    }
    @Test
    void testUpdateTagsNotFound() throws Exception {
        when(noteRepository.findById(99999L)).thenThrow(new IllegalArgumentException("Note not found for ID: " + 99999L));
        Set<String> tagNames = new HashSet<>(Arrays.asList("Tag 1", "Tag 2"));
        var json = new ObjectMapper().writeValueAsString(tagNames);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/{id}/tags", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testUpdateTagsNewTags() throws Exception {
        when(tagRepository.findByName("Tag 1")).thenReturn(Optional.empty());
        when(tagRepository.save(Mockito.any(Tag.class))).thenReturn(tag1);
        when(tagRepository.findByName("Tag 2")).thenReturn(Optional.of(tag2));
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteRepository.save(Mockito.any(Note.class))).thenReturn(note);

        Set<String> tagNames = new HashSet<>(Arrays.asList("Tag 1", "Tag 2"));
        var json = new ObjectMapper().writeValueAsString(tagNames);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/{id}/tagsUpdate", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tags.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tags[*].name").value(Matchers.containsInAnyOrder("Tag 1", "Tag 2")));
    }

    @Test
    void testUpdateTagsInternalServerError() throws Exception {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(tagRepository.findByName("Tag 1")).thenReturn(Optional.ofNullable(tag1));
        when(tagRepository.findByName("Tag 2")).thenReturn(Optional.ofNullable(tag2));

        when(noteRepository.save(Mockito.any(Note.class)))
                .thenThrow(new RuntimeException("Database error"));

        Set<String> tagNams = new HashSet<>(Arrays.asList("Tag 1","Tag 2"));
        var json = new ObjectMapper().writeValueAsString(tagNams);
        mockMvc.perform(MockMvcRequestBuilders.put("/api/notes/{id}/tagsUpdate", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    void testGetAllNotes() throws Exception {
        List<Note> notes = Arrays.asList(note, note2);
        when(noteRepository.findAll()).thenReturn(notes);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/fetch"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("Test Note"));
    }

    /**
     * Tests the delete endpoint, expect no content after deletion.
     *
     * @throws Exception
     */
    @Test
    void testDeleteNoteSuccess() throws Exception{
        when(noteRepository.existsById(1L)).thenReturn(true);

        //this simulates successful delete. doNothing() essentially indicates
        //to Mockito that nothing needs to be done for the action to be triggered.
        //it is as if it was a regular line in the code (but only done in mocking).
        doNothing().when(noteRepository).deleteById(1L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/notes/delete/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testDeleteNoteNotFound() throws Exception {
        when(noteRepository.existsById(99999L)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/notes/delete/{id}", 99999L))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testGetNoteByIdSuccessful() throws Exception{
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/notes/{id}", 1L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Test Note"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body").value("Test Note"));
    }
}