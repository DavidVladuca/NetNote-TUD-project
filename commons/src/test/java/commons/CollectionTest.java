package commons;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionTest {
    private Collection collection1;
    private Collection collection2;
    private Collection differentCollection;
    private Collection emptyCollection;
    private Server server1;

    @BeforeEach
    public void setUp() {
        server1 = new Server();
        Server server2 = new Server();
        collection1 = new Collection(server1, "Test Collection 1");
        collection2 = new Collection(server1, "Test Collection 1");
        differentCollection = new Collection(server2, "Test Collection 2");
        emptyCollection = new Collection(server1, "Empty Collection");

    }

    @Test
    public void testEquals_SameId_ReturnsFalse() {
        collection1.setCollectionId(1);
        collection2.setCollectionId(1);
        assertEquals(collection1.hashCode(), collection2.hashCode(), "Collections with the same ID should have the same hash code");
    }

    @Test
    public void testEquals_DifferentId_ReturnsFalse() {
        collection1.setCollectionId(1);
        differentCollection.setCollectionId(2);
        assertNotEquals(collection1, differentCollection, "Collections with different IDs should not be equal");
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        assertNotEquals(collection1, null, "A collection should not be equal to null");
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        String randomObject = "Not a Collection!";
        assertNotEquals(collection1, randomObject, "A collection should not be equal to an object of a different class");
    }

    @Test
    public void testHashCode_SameId_ReturnsSameHashCode() {
        assertEquals(collection1.hashCode(), collection2.hashCode(), "Collections with the same attributes should have the same hash code");
    }
    /*
    @Test
    public void testToString_WithNotes() {
        String expectedOutput = """
                Collection:
                Collection ID: %d
                Server ID: 100
                Collection Title: Test Collection 1
                Notes:

                Note:
                Note ID: 0
                Collection ID: %d
                Title: Note 1 Title
                Body:
                Note 1 Body
                Tags:
                ---------------------------
                Note:
                Note ID: 0
                Collection ID: %d
                Title: Note 2 Title
                Body:
                Note 2 Body
                Tags:
                """.formatted(collection1.getCollectionId(), collection1.getCollectionId(), collection1.getCollectionId());

        assertEquals(expectedOutput.trim(), collection1.toString().trim(), "toString should match the expected format with notes");
    }
*/
    @Test
    public void testToString_EmptyCollection() {
        String expectedOutput = """
                Collection:
                Collection ID: %d
                Server ID: %d
                Collection Title: Empty Collection
                Notes:

                No Notes
                """.formatted(emptyCollection.getCollectionId(), server1.getServerId());

        assertEquals(expectedOutput.trim(), emptyCollection.toString().trim(), "toString should match the expected format for an empty collection");
    }
    @Test
    public void testSearchEmpty(){
        Collection testCollection = new Collection();
        Note note1 = new Note();
        Note note2 = new Note();
        note1.setBody("Some body");
        note2.setBody("Some other body");
        testCollection.addNote(note1);
        testCollection.addNote(note2);
        assertEquals(new ArrayList<>(), testCollection.getSearch(""));
    }
    @Test
    public void testSearchNoMatch(){
        Collection testCollection = new Collection();
        ArrayList<ArrayList<Long>> expected_output = new ArrayList<>();
        ArrayList<Long> expected_first_AL = new ArrayList<>();
        expected_first_AL.add(-1L);
        expected_output.add(expected_first_AL);
        Note note1 = new Note();
        Note note2 = new Note();
        note1.setBody("Some body");
        note2.setBody("Some other body");
        testCollection.addNote(note1);
        testCollection.addNote(note2);
        assertEquals(expected_output, testCollection.getSearch("Not in body"));
    }
    @Test
    public void testSearchMultipleNoteMatch(){
        Collection testCollection = new Collection();
        ArrayList<ArrayList<Long>> expected_output = new ArrayList<>();
        ArrayList<Long> expected_first_AL = new ArrayList<>();
        ArrayList<Long> expected_second_AL = new ArrayList<>();
        expected_first_AL.add(1L);
        expected_first_AL.add(11L);
        expected_second_AL.add(27L);
        expected_second_AL.add(17L);
        expected_second_AL.add(43L);
        expected_output.add(expected_first_AL);
        expected_output.add(expected_second_AL);

        Note note1 = new Note();
        Note note2 = new Note();
        note1.setNoteId(1L);
        note2.setNoteId(27L);
        note1.setBody("Some body");
        note2.setBody("Some other body (adding extra match: body)");
        note1.setTitle("Note 1");
        note2.setTitle("Note 2");
        testCollection.addNote(note1);
        testCollection.addNote(note2);
        assertEquals(expected_output, testCollection.getSearch("body"));
    }

    @Test
    public void testAddNote() {
        // Creating new notes
        Note note1 = new Note("Title 1", "Body 1", collection1);
        Note note2 = new Note("Title 2", "Body 2", collection1);

        // Verifying the collection starts empty
        assertTrue(collection1.getNotes().isEmpty(), "Collection should start with no notes");

        // Adding the first note
        collection1.addNote(note1);

        // Verifying the note was added
        assertEquals(1, collection1.getNotes().size(), "Collection should have one note after adding");
        assertTrue(collection1.getNotes().contains(note1), "Collection should contain the added note");

        // Adding the second note
        collection1.addNote(note2);

        // Verifying both notes are present
        assertEquals(2, collection1.getNotes().size(), "Collection should have two notes after adding");
        assertTrue(collection1.getNotes().contains(note2), "Collection should contain the second note");
    }

    @Test
    public void testRemoveNote() {
        // Creating new notes
        Note note1 = new Note("Title 1", "Body 1", collection1);
        note1.setNoteId(1);
        Note note2 = new Note("Title 2", "Body 2", collection1);
        note2.setNoteId(2);

        // Adding the notes to the collection
        collection1.addNote(note1);
        collection1.addNote(note2);

        // Verifying both notes are present
        assertEquals(2, collection1.getNotes().size(), "Collection should initially contain two notes");

        // Removing the first note
        collection1.removeNote(note1);

        // Verifying the first note was removed and the second note remains
        assertEquals(1, collection1.getNotes().size(), "Collection should contain one note after removal");
        assertTrue(!collection1.getNotes().contains(note1), "Collection should not contain the removed note");
        assertTrue(collection1.getNotes().contains(note2), "Collection should still contain the remaining note");

        // Removing the second note
        collection1.removeNote(note2);

        // Verifying the collection is now empty
        assertTrue(collection1.getNotes().isEmpty(), "Collection should be empty after removing all notes");
    }
}
