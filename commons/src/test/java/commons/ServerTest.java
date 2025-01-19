package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ServerTest {
    private Collection collection1;
    private Server server1;
    private Server server2;

    @BeforeEach
    public void setUp() {
        server1 = new Server();
        server2 = new Server();
        collection1 = new Collection(server1, "Test Collection 1",
                "collection-1", false);
    }

    @Test
    public void getterAndSetterTest() {
        server1.setServerId(3);
        long expected = 3;
        long actual = server1.getServerId();
        assertEquals(expected, actual);
    }

    @Test
    public void equalsSameParametersTest() {
        server1.setServerId(3);
        server2.setServerId(3);
        server1.addCollection(collection1);
        server2.addCollection(collection1);
        assertEquals(server1, server2);
    }

    @Test
    public void equalsSameTest() {
        server1.setServerId(3);
        server1.addCollection(collection1);
        assertEquals(server1, server1);
    }

    @Test
    public void equalsNullTest() {
        server1.setServerId(3);
        server2 = null;
        server1.addCollection(collection1);
        assertNotEquals(server1, server2);
    }

    @Test
    public void equalsHashCodeSameParametersTest() {
        server1.setServerId(3);
        server2.setServerId(3);
        server1.addCollection(collection1);
        server2.addCollection(collection1);
        assertEquals(server1.hashCode(), server2.hashCode());
    }

    @Test
    public void toStringTest() {
        server1.setServerId(3);
        server1.addCollection(collection1);
        server1.addCollection(collection1);
        String actual = server1.toString();
        String expected = "Server:\n" +
                "Server ID: 3\n" +
                "Collections:\n" +
                "\n" +
                "Collection:\n" +
                "Collection ID: 0\n" +
                "Server ID: 3\n" +
                "Collection Title: Test Collection 1\n" +
                "Collection Path: collection-1\n" +
                "Default Collection: false\n" +
                "Notes:\n" +
                "\n" +
                "No Notes\n" +
                "#########################\n" +
                "Collection:\n" +
                "Collection ID: 0\n" +
                "Server ID: 3\n" +
                "Collection Title: Test Collection 1\n" +
                "Collection Path: collection-1\n" +
                "Default Collection: false\n" +
                "Notes:\n" +
                "\n" +
                "No Notes";
        assertEquals(expected, actual);
    }

    @Test
    void getURL() {
        assertEquals(server1.getURL(), "http://localhost:8080");
    }

    @Test
    void setURL() {
        server1.setURL("http://localhost:8081");
        assertEquals(server1.getURL(), "http://localhost:8081");
    }
}
