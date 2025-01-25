package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageTest {

    @Test
    public void testConstructor_ValidInputs() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals(1, language.getId(), "Constructor should set id correctly");
        assertEquals("English", language.getName(), "Constructor should set name correctly");
        assertEquals("EN", language.getAbbr(), "Constructor should set abbreviation correctly");
        assertEquals("/path/to/flag.png", language.getImgPath(), "Constructor should set image path correctly");
    }

    @Test
    public void testConstructor_InvalidAbbr_Length() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                new Language(1, "English", "ENG", "/path/to/flag.png"));
        assertEquals("Abbreviation must be two characters long (use ISO 639).", exception.getMessage());
    }

    @Test
    public void testGetId() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals(1, language.getId(), "getId should return the correct id");
    }

    @Test
    public void testGetName() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals("English", language.getName(), "getName should return the correct name");
    }

    @Test
    public void testGetAbbr() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals("EN", language.getAbbr(), "getAbbr should return the correct abbreviation");
    }

    @Test
    public void testGetImgPath() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals("/path/to/flag.png", language.getImgPath(), "getImgPath should return the correct image path");
    }

    @Test
    public void testSetId() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        language.setId(2);
        assertEquals(2, language.getId(), "setId should update the id correctly");
    }

    @Test
    public void testEqualsSameObject() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals(language, language, "An object should equal itself");
    }

    @Test
    public void testEqualsDifferentObject_SameValues() {
        Language language1 = new Language(1, "English", "EN", "/path/to/flag.png");
        Language language2 = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals(language1, language2, "Objects with the same values should be equal");
    }

    @Test
    public void testEqualsDifferentId() {
        Language language1 = new Language(1, "English", "EN", "/path/to/flag.png");
        Language language2 = new Language(2, "English", "EN", "/path/to/flag.png");
        assertNotEquals(language1, language2, "Objects with different ids should not be equal");
    }

    @Test
    public void testEqualsNull() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertNotEquals(language, null, "An object should not equal null");
    }

    @Test
    public void testEqualsDifferentClass() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        assertNotEquals(language, "NotALanguage", "An object should not equal an object of a different class");
    }

    @Test
    public void testHashCodeSameValues() {
        Language language1 = new Language(1, "English", "EN", "/path/to/flag.png");
        Language language2 = new Language(1, "English", "EN", "/path/to/flag.png");
        assertEquals(language1.hashCode(), language2.hashCode(), "Objects with the same values should have the same hash code");
    }

    @Test
    public void testHashCodeDifferentValues() {
        Language language1 = new Language(1, "English", "EN", "/path/to/flag.png");
        Language language2 = new Language(2, "Spanish", "ES", "/path/to/flag2.png");
        assertNotEquals(language1.hashCode(), language2.hashCode(), "Objects with different values should have different hash codes");
    }

    @Test
    public void testToString() {
        Language language = new Language(1, "English", "EN", "/path/to/flag.png");
        String expectedString = "Language{id=1, name='English', abbr='EN', img_path='/path/to/flag.png'}";
        assertEquals(expectedString, language.toString(), "toString should return the correct string representation");
    }
}