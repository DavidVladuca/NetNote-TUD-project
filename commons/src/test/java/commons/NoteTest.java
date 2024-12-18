package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NoteTest {

    private Note note1;
    private Note note2;
    private Note differentNote;
    private Collection collection1;

    @BeforeEach
    public void setUp() {
        collection1 = new Collection();
        Collection collection2 = new Collection();
        note1 = new Note("Title 1", "Test Content 1", collection1);
        note2 = new Note("Title 1", "Test Content 1", collection1);
        differentNote = new Note("Title 2", "Test Content 2", collection2);
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
        // Add tags to the note
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        tag1.setTagId(1);
        tag2.setTagId(2);

        Set<Tag> expectedTags = new LinkedHashSet<>();
        expectedTags.add(tag1);
        expectedTags.add(tag2);

        note1.setTags(expectedTags);

        note1.setNoteId(1);
        String expectedOutput = """
                Note:
                Note ID: 1
                Collection ID: %d
                Title: Title 1
                Body:
                Test Content 1
                Tags: Tag1, Tag2
                """.formatted(collection1.getCollectionId());
        assertEquals(expectedOutput.trim(), note1.toString().trim(), "toString output should match the expected format");
    }

    @Test
    public void testGetMatchIndexBeginningChar(){
        ArrayList<Long> expected = new ArrayList<>();
        expected.add(0L);
        expected.add(7L);
        assertEquals(expected, note1.getMatchIndices("T"));
    }
    //Todo - add test for full word, and test for searching wrong word.

    @Test
    void getNoteId() {
        // Test getting the note ID
        note1.setNoteId(5);
        assertEquals(5, note1.getNoteId(), "getNoteId should return the correct ID");
    }

    @Test
    void setNoteId() {
        // Test setting the note ID
        note1.setNoteId(10);
        assertEquals(10, note1.getNoteId(), "setNoteId should correctly set the ID");
    }

    @Test
    void getTitle() {
        // Test getting the title
        assertEquals("Title 1", note1.getTitle(), "getTitle should return the correct title");
    }

    @Test
    void setTitle() {
        // Test setting the title
        note1.setTitle("New Title");
        assertEquals("New Title", note1.getTitle(), "setTitle should correctly set the title");
    }

    @Test
    void getBody() {
        // Test getting the body content
        assertEquals("Test Content 1", note1.getBody(), "getBody should return the correct body");
    }

    @Test
    void setBody() {
        // Test setting the body content
        note1.setBody("New Body");
        assertEquals("New Body", note1.getBody(), "setBody should correctly set the body");
    }

    @Test
    void getCollection() {
        // Test getting the collection of the note
        assertEquals(collection1, note1.getCollection(), "getCollection should return the correct collection");
    }

    @Test
    void setCollection() {
        // Test setting the collection of the note
        Collection newCollection = new Collection();
        note1.setCollection(newCollection);
        assertEquals(newCollection, note1.getCollection(), "setCollection should correctly set the collection");
    }

    @Test
    void getTags() {
        // Set up tags and add them to the note
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        tag1.setTagId(1);
        tag2.setTagId(2);
        Set<Tag> expectedTags = Set.of(tag1, tag2);
        note1.setTags(expectedTags);

        // Test retrieving tags
        assertEquals(expectedTags, note1.getTags(), "getTags should return the correct set of tags");
    }

    @Test
    void setTags() {
        // Set up new tags and assign them to the note
        Tag tag1 = new Tag("Tag1");
        Tag tag2 = new Tag("Tag2");
        tag1.setTagId(1);
        tag2.setTagId(2);
        Set<Tag> newTags = Set.of(tag1, tag2);
        note1.setTags(newTags);

        // Test if the tags were correctly assigned
        assertEquals(newTags, note1.getTags(), "setTags should correctly set the tags");
    }

}
