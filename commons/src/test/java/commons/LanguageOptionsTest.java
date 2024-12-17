package commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LanguageOptionsTest {
    @Test
    public void checkConstructor() {
        var lo = new LanguageOptions();
        var l = new Language(0, "English", "EN",
                "Flags/English_flag.png");
        assertEquals(l, lo.getCurrentLanguage());
    }

    @Test
    public void addLanguageTest() {
        var lo = new LanguageOptions();
        var polish = new Language(2, "Polish", "PL",
                "Flags/Polish_flag.png");
        var english = new Language(0, "English", "EN", "Flags/English_flag.png"); //make default
        var spanish = new Language(1, "Spanish", "ES", "Flags/Spanish_flag.png");
        lo.addLanguage(polish);
        ArrayList<Language> languages = new ArrayList<>();
        languages.add(english);
        languages.add(spanish);
        languages.add(polish);

        assertEquals(languages.toString(), lo.getLanguages().toString());
    }

    @Test
    public void getLanguageFromAbbrTest() {
        var lo = new LanguageOptions();
        var l = new Language(0, "English", "EN",
                "Flags/English_flag.png");
        assertEquals(l, lo.getLanguageFromAbbr("EN"));
    }

}
