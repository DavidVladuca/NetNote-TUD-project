package commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LanguageTest {

    @Test
    public void checkConstructor() {
        var l = new Language(1, "Polish",
                "PL", "some/path");
        assertEquals(1, l.getId());
        assertEquals("Polish", l.getName());
        assertEquals("PL", l.getAbbr());
        assertEquals("some/path", l.getImg_path());
    }

    @Test
    public void equalsHashCode() {
        var l1 = new Language(1, "Polish",
                "PL", "some/path");
        var l2 = new Language(1, "Polish",
                "PL", "some/path");
        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    public void notEqualsHashCode() {
        var l1 = new Language(1, "Polish",
                "PL", "some/path");
        var l2 = new Language(2, "Romanian",
                "RO", "some/path");
        assertNotEquals(l1, l2);
        assertNotEquals(l1.hashCode(), l2.hashCode());
    }

    @Test
    public void hasToString() {
        var actual = new Language(1, "Polish",
                "PL", "some/path").toString();
        assertTrue(actual.contains(Language.class.getSimpleName()));
        assertTrue(actual.contains("id"));
        assertTrue(actual.contains("name"));
        assertTrue(actual.contains("abbr"));
        assertTrue(actual.contains("screen_path"));
    }
}
