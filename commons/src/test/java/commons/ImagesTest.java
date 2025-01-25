package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ImagesTest {

    private Images image1;
    private Images image2;
    private Note note1;
    private Note note2;

    @BeforeEach
    public void setUp() {
        note1 = new Note();
        note1.setNoteId(1L);
        note2 = new Note();
        note2.setNoteId(2L);

        image1 = new Images(1L, "Image1", new byte[]{1, 2, 3}, note1);
        image2 = new Images(2L, "Image2", new byte[]{4, 5, 6}, note2);
    }

    @Test
    public void testDefaultConstructor() {
        Images defaultImage = new Images();
        assertNull(defaultImage.getId(), "Default constructor should set id to null");
        assertNull(defaultImage.getName(), "Default constructor should set name to null");
        assertNull(defaultImage.getData(), "Default constructor should set data to null");
        assertNull(defaultImage.getNote(), "Default constructor should set note to null");
        assertNull(defaultImage.getMimeType(), "Default constructor should set mimeType to null");
        assertNull(defaultImage.getFileUrl(), "Default constructor should set fileUrl to null");
    }

    @Test
    public void testParameterizedConstructor() {
        assertEquals(1L, image1.getId(), "Parameterized constructor shuld set id correctly");
        assertEquals("Image1", image1.getName(), "Parameterized constructor should set name correctly");
        assertArrayEquals(new byte[]{1, 2, 3}, image1.getData(), "Parameterized constructor should set data correctly");
        assertEquals(note1, image1.getNote(), "Parameterized constructor should set note correctly");
    }

    @Test
    public void testGettersAndSetters() {
        image1.setId(3L);
        assertEquals(3L, image1.getId(), "setId should set id correctly");

        image1.setName("UpdatedName");
        assertEquals("UpdatedName", image1.getName(),
                "setName should set name correctly");

        byte[] updatedData = new byte[]{7, 8, 9};
        image1.setData(updatedData);
        assertArrayEquals(updatedData, image1.getData(),
                "setData should set data correctly");

        image1.setNote(note2);
        assertEquals(note2, image1.getNote(),
                "setNote should set note correctly");

        image1.setMimeType("image/png");
        assertEquals("image/png", image1.getMimeType(),
                "setMimeType should set mimeType correctly");

        image1.setFileUrl("http://example.com/image.png");
        assertEquals("http://example.com/image.png", image1.getFileUrl(),
                "setFileUrl should set fileUrl correctly");
    }

    @Test
    public void testEquals_SameObject() {
        assertEquals(image1, image1, "An object should equal itself");
    }

    @Test
    public void testEquals_DifferentObject_SameId() {
        Images sameIdImage = new Images(1L, "DifferentName", new byte[]{}, note1);
        assertEquals(image1, sameIdImage, "Images with the same id should be equal");
    }

    @Test
    public void testEquals_DifferentId() {
        assertNotEquals(image1, image2, "Images w different ids should not be equal");
    }

    @Test
    public void testEquals_Null() {
        assertNotEquals(image1, null, "An image should not equal null");
    }

    @Test
    public void testEquals_DifferentClass() {
        assertNotEquals(image1, "NotAnImage",
                "An image shouldnt equal an object of a different class");
    }

    @Test
    public void testHashCode_SameId() {
        Images sameIdImage = new Images(1L, "DifferentName", new byte[]{}, note1);
        assertEquals(image1.hashCode(), sameIdImage.hashCode(),
                "Images with the same id should have the same hash code");
    }

    @Test
    public void testHashCode_DifferentId() {
        assertNotEquals(image1.hashCode(), image2.hashCode(),
                "Images with different ids should have different hash codes");
    }

    @Test
    public void testToString() {
        String expectedString = "Image{" +
                "id=1, name='Image1', data=" + Arrays.toString(new byte[]{1, 2, 3}) +
                ", note=" + note1 +
                '}';
        assertEquals(expectedString, image1.toString(),
                "toString should return the correct string representation");
    }

    @Test
    public void testSetDataNull() {
        image1.setData(null);
        assertNull(image1.getData(), "setData should handle null input for data");
    }

    @Test
    public void testSetNoteNull() {
        image1.setNote(null);
        assertNull(image1.getNote(), "setNote should handle null input for note");
    }

    @Test
    public void testEqualsSameIdDifferentData() {
        Images sameIdDifferentData = new Images(1L,
                "SameIdDifferentData", new byte[]{10, 11, 12}, note1);
        assertEquals(image1, sameIdDifferentData,
                "Images with the same ID but different data should still be equal");
    }

    @Test
    public void testFileUrlGetterSetter() {
        image1.setFileUrl("http://localhost/file.jpg");
        assertEquals("http://localhost/file.jpg",
                image1.getFileUrl(), "setFileUrl and getFileUrl should work correctly");
    }
}