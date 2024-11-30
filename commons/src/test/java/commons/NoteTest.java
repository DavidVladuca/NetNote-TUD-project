package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    private Note note1;
    private Note note2;
    private Note differentNote;
    private Collection collection1;
    private User user1;

    @BeforeEach
    public void setUp() {
        collection1 = new Collection();
        Collection collection2 = new Collection();
        user1 = new User();
        User user2 = new User();
        note1 = new Note("Title 1", "Test Content 1", collection1, user1);
        note2 = new Note("Title 1", "Test Content 1", collection1, user1);
        differentNote = new Note("Title 2", "Test Content 2", collection2, user2);
    }

    @Test
    public void testEquals_SameIds_ReturnsTrue() {
        note1.setNoteId(1);
        note2.setNoteId(1);
        assertEquals(note1, note2, "Notes with the same ID should be equal");
    }

    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        note1.setNoteId(1);
        differentNote.setNoteId(2);
        assertNotEquals(note1, differentNote, "Notes with different IDs should not be equal");
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        assertNotEquals(note1, null, "A note should not be equal to null");
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        String randomObject = "Not a Note!";
        assertNotEquals(note1, randomObject, "A note should not be equal to an object of a different class");
    }

    @Test
    public void testHashCode_SameIds_ReturnsSameHashCode() {
        note1.setNoteId(1);
        note2.setNoteId(1);
        assertEquals(note1.hashCode(), note2.hashCode(), "Notes with the same ID should have the same hash code");
    }

    @Test
    public void testHashCode_DifferentIds_ReturnsDifferentHashCode() {
        note1.setNoteId(1);
        differentNote.setNoteId(2);
        assertNotEquals(note1.hashCode(), differentNote.hashCode(), "Notes with different IDs should have different hash codes");
    }

    @Test
    public void testToString() {
        note1.setNoteId(1);
        String expectedOutput = """
                Note:
                Note ID: 1
                Collection ID: %d
                Title: Title 1
                Body:
                Test Content 1
                Tags:
                """.formatted(collection1.getCollectionId());
        assertEquals(expectedOutput.trim(), note1.toString().trim(), "toString output should match the expected format");
    }
}
