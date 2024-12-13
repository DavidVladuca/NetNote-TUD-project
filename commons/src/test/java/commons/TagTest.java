package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    private Tag tag1;
    private Tag tag2;
    private Tag differentTag;

    @BeforeEach
    public void setUp() {
        tag1 = new Tag("Tag1");
        tag2 = new Tag("Tag1");
        differentTag = new Tag("Tag2");
    }

    @Test
    public void testEquals_SameIds_ReturnsTrue() {
        tag1.setTagId(1);
        tag2.setTagId(1);
        assertEquals(tag1, tag2, "Tags with the same ID should be equal");
    }

    @Test
    public void testEquals_DifferentIds_ReturnsFalse() {
        tag1.setTagId(1);
        differentTag.setTagId(2);
        assertNotEquals(tag1, differentTag, "Tags with different IDs should not be equal");
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        assertNotEquals(tag1, null, "A tag should not be equal to null");
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        String randomObject = "Not a Tag!";
        assertNotEquals(tag1, randomObject, "A tag should not be equal to an object of a different class");
    }

    @Test
    public void testToString() {
        tag1.setTagId(1);
        String expectedOutput = "(ID - 1, Name - Tag1)";
        assertEquals(expectedOutput, tag1.toString(), "toString output should match the expected format");
    }

    //Todo - hashcode tests
}