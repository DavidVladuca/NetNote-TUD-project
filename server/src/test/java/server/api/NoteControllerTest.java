package server.api;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.NoteController;
import server.database.CollectionRepository;
import server.database.NoteRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteControllerTest {
    private NoteController noteController;

    @BeforeEach
    public void setUp() {
        noteController = new NoteController(null, null, null);
    }

    @Test
    public void indexReturnsHelloWorld() {
        var expected = "Hello world!";
        var actual = noteController.index();
        assertEquals(expected, actual);
    }
}
