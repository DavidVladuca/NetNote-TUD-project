package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ImagesTest {
    private Images image1;
    private Images image2;
    private Note note;

    @BeforeEach
    public void setUp() {
        note = new Note("Title 2", "Test Content 2", new Collection());
        image1 = new Images(0L, "name", new byte[]{0, 0, 0}, note);
        image2 = new Images(0L, "name", new byte[]{0, 0, 0}, note);
    }

    @Test
    public void testEquals_SameIds_ReturnsTrue() {
        image1.setId(1L);
        image2.setId(1L);
        assertEquals(image1, image2, "Images with the same ID should be equal");
    }

    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        image1.setId(1L);
        image2.setId(2L);
        assertNotEquals(image1, image2, "Images with different IDs should not be equal");
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        assertNotEquals(image1, null, "An image should not be equal to null");
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        String randomObject = "Not a Note!";
        assertNotEquals(image1, randomObject, "An image should not be equal to an object of a different class");
    }

    @Test
    public void testHashCode_SameIds_ReturnsSameHashCode() {
        image1.setId(1L);
        image2.setId(1L);
        assertEquals(image1.hashCode(), image2.hashCode(), "Images with the same ID should have the same hash code");
    }

    @Test
    public void testHashCode_DifferentIds_ReturnsDifferentHashCode() {
        image1.setId(1L);
        image2.setId(2L);
        assertNotEquals(image1.hashCode(), image2.hashCode(), "Notes with different IDs should have different hash codes");
    }

    @Test
    public void testToString() {
        String expectedOutput = "Image{id=0, name='name', data=[0, 0, 0], note=Note:\n" +
                "Note ID: 0\n" +
                "Collection ID: 0\n" +
                "Title: Title 2\n" +
                "Body:\n" +
                "Test Content 2\n" +
                "Tags: \n" +
                "}";
        assertEquals(expectedOutput, image1.toString());
    }
}