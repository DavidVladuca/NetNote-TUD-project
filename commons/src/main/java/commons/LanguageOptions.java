package commons;

import java.util.ArrayList;

public class LanguageOptions {
    //todo - missing a lot of tests and javadocs
    ArrayList<Language> lang_options;
    Language english = new Language(0, "English", "EN", "client/src/main/resources/client/homeScreen.fxml"); //make default
    Language spanish = new Language(1, "Spanish", "ES", "client/src/main/resources/client/homeScreen.fxml");
    int current_index;

    private static LanguageOptions single_instance = null;

    /**
     * constructor for language array
     */
    public LanguageOptions() {
        lang_options = new ArrayList<>();
        lang_options.add(english); //default language
        lang_options.add(spanish);
        current_index = 0;
    }

    public static synchronized LanguageOptions getInstance(){
        if (single_instance==null)
            single_instance = new LanguageOptions();
        return single_instance;
    }

    public Language getCurrentLanguage() {
        return lang_options.get(current_index);
    }

    public void setCurrent_index(int new_index) {
        current_index = new_index;
    }

    public void addLanguage(Language new_language) throws IllegalArgumentException {
        for (int i = 0; i < lang_options.size(); i++) {
            if (lang_options.get(i).getId() == new_language.getId()) {
                throw new IllegalArgumentException("New language ID cannot be the same as option " + lang_options.get(i).toString()); //todo - check if toString for language is implemented
            }
        }
        lang_options.add(new_language);
    }

    public ArrayList<Language> getLanguages() {
        return lang_options;
    }

    public ArrayList<Language> getAllAbbreviations() {
        ArrayList<Language> languages_abbrs = new ArrayList<Language>();
        for (int i = 0; i < lang_options.size(); i++) {
            languages_abbrs.set(i, lang_options.get(i));
        }
        return languages_abbrs;
    }

    public Language getLanguageFromAbbr(String abbr) throws IllegalArgumentException{
        for (int i = 0; i < lang_options.size(); i++) {
            if (lang_options.get(i).getAbbr().equals(abbr)){
                return lang_options.get(i);
            }
        }
        throw new IllegalArgumentException("Abbreviation "+abbr+ "not found");
    }

}
