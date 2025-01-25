package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TagTest {

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    public void setUp() {
        tag1 = new Tag("Tag1");
        tag1.setTagId(1L);

        tag2 = new Tag("Tag2");
        tag2.setTagId(2L);
    }

    @Test
    public void testDefaultConstructor() {
        Tag tag = new Tag();
        assertNotNull(tag, "Default constructor should create a Tag object");
        assertNull(tag.getName(), "Default constructor should initialize name to null");
        assertTrue(tag.getNotes().isEmpty(), "Default constructor should initialize notes as an empty set");
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals("Tag1", tag1.getName(),
                "Parameterized constructor should set the name correctly");
    }

    @Test
    public void testGetTagId() {
        assertEquals(1L, tag1.getTagId(), "getTagId should return the correct tag ID");
    }

    @Test
    public void testSetTagId() {
        tag1.setTagId(10L);
        assertEquals(10L, tag1.getTagId(), "setTagId should update the tag ID correctly");
    }

    @Test
    public void testGetName() {
        assertEquals("Tag1", tag1.getName(), "getName should return the correct tag name");
    }

    @Test
    public void testSetName() {
        tag1.setName("NewTag1");
        assertEquals("NewTag1", tag1.getName(), "setName should update the tag name correctly");
    }

    @Test
    public void testGetNotes() {
        Set<Note> notes = new HashSet<>();
        Note note = new Note();
        note.setNoteId(1L);
        notes.add(note);

        tag1.setNotes(notes);
        assertEquals(notes, tag1.getNotes(), "getNotes should return the correct set of notes");
    }

    @Test
    public void testSetNotes() {
        Set<Note> notes = new HashSet<>();
        Note note = new Note();
        note.setNoteId(1L);
        notes.add(note);

        tag1.setNotes(notes);
        assertEquals(1, tag1.getNotes().size(),
                "setNotes should update the notes associated with the tag");
        assertTrue(tag1.getNotes().contains(note),
                "The notes set should contain the added note");
    }

    @Test
    public void testEquals_SameObject() {
        assertEquals(tag1, tag1, "An object should equal itself");
    }

    @Test
    public void testEquals_DifferentObject_SameName() {
        Tag sameNameTag = new Tag("Tag1");
        assertEquals(tag1, sameNameTag, "Tags w the same name should be equal");
    }

    @Test
    public void testEquals_DifferentName() {
        assertNotEquals(tag1, tag2, "Tags w different names should not be equal");
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(tag1, null, "Tag should not equal null");
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(tag1, "NotATag", "A tag should not equal an object of a different class");
    }

    @Test
    public void testHashCode_SameName() {
        Tag sameNameTag = new Tag("Tag1");
        assertEquals(tag1.hashCode(), sameNameTag.hashCode(),
                "Tags w the same name should have the same hash code");
    }

    @Test
    public void testHashCode_DifferentName() {
        assertNotEquals(tag1.hashCode(), tag2.hashCode(),
                "Tags w different names should have different hash codes");
    }

    @Test
    public void testToString() {
        String expectedString = "(ID - 1, Name - Tag1)";
        assertEquals(expectedString, tag1.toString(),
                "toString should return the correct string representation");
    }
}