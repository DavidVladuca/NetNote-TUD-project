package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageOptionsTest {

    private LanguageOptions languageOptions;

    @BeforeEach
    public void setUp() {
        languageOptions = new LanguageOptions();
    }

    @Test
    public void testSingletonInstance() {
        LanguageOptions instance1 = LanguageOptions.getInstance();
        LanguageOptions instance2 = LanguageOptions.getInstance();

        assertNotNull(instance1, "Singleton instance should not be null");
        assertSame(instance1, instance2, "Singleton should always return the same instance");
    }

    @Test
    public void testDefaultConstructor() {
        ArrayList<Language> languages = languageOptions.getLanguages();

        assertEquals(4, languages.size(), "Default constructor should initialize 4 languages");

        assertEquals("English", languages.get(0).getName(), "First language should be English");
        assertEquals("Spanish", languages.get(1).getName(), "Second language should be Spanish");
        assertEquals("Dutch", languages.get(2).getName(), "Third language should be Dutch");
        assertEquals("Gibberish", languages.get(3).getName(), "Fourth language should be Gibberish");
    }

    @Test
    public void testGetCurrentLanguage() {
        Language currentLanguage = languageOptions.getCurrentLanguage();
        assertEquals("English", currentLanguage.getName(),
                "Default current language should be English");
    }

    @Test
    public void testSetCurrentIndex() {
        languageOptions.setCurrentIndex(1);
        assertEquals("Spanish", languageOptions.getCurrentLanguage().getName(),
                "Setting index to 1 should make Spanish the current language");
    }

    @Test
    public void testAddLanguage_Valid() {
        Language newLanguage = new Language(4, "German", "DE", "Flags/German_flag.png");

        languageOptions.addLanguage(newLanguage);

        ArrayList<Language> languages = languageOptions.getLanguages();
        assertEquals(5, languages.size(), "Adding a new language should increase the size");
        assertEquals("German", languages.get(4).getName(), "The added language should be German");
    }

    @Test
    public void testAddLanguage_DuplicateId() {
        Language duplicateLanguage = new Language(0, "DuplicateEnglish", "EN", "Flags/Duplicate_flag.png");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                languageOptions.addLanguage(duplicateLanguage));

        assertTrue(exception.getMessage().contains("New language ID cannot be the same as option"),
                "Adding a language with a duplicate ID should throw an exception");
    }

    @Test
    public void testGetLanguages() {
        ArrayList<Language> languages = languageOptions.getLanguages();

        assertEquals(4, languages.size(), "getLanguages should return all available languages");
        assertEquals("English", languages.get(0).getName(),
                "The first language should be English");
    }

    @Test
    public void testGetAllAbbreviations() {
        ArrayList<Language> abbreviations = languageOptions.getAllAbbreviations();

        assertEquals(4, abbreviations.size(), "getAllAbbreviations should return all languages");
        assertEquals("EN", abbreviations.get(0).getAbbr(), "The first abbreviation should be EN");
        assertEquals("ES", abbreviations.get(1).getAbbr(), "The second abbreviation should be ES");
        assertEquals("NL", abbreviations.get(2).getAbbr(), "The third abbreviation should be NL");
        assertEquals("ZZ", abbreviations.get(3).getAbbr(), "The fourth abbreviation should be ZZ");
    }

    @Test
    public void testGetLanguageFromAbbr_Valid() {
        Language language = languageOptions.getLanguageFromAbbr("ES");
        assertEquals("Spanish", language.getName(),
                "getLanguageFromAbbr should return the correct language for valid abbreviation");
    }

    @Test
    public void testGetLanguageFromAbbr_Invalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                languageOptions.getLanguageFromAbbr("XX"));

        assertEquals("Abbreviation XXnot found", exception.getMessage(),
                "getLanguageFromAbbr should throw an exception for an invalid abbreviation");
    }
}