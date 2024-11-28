package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
