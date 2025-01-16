package commons;

import java.util.ArrayList;

public class LanguageOptions {
    //todo - missing a lot of tests and javadocs
    ArrayList<Language> langOptions;
    Language english = new Language(0, "English", "EN", "Flags/English_flag.png");
    Language spanish = new Language(1, "Spanish", "ES", "Flags/Spanish_flag.png");
    Language dutch = new Language(2, "Dutch", "NL", "Flags/Dutch_flag.png");
    Language gibberish = new Language(3, "Gibberish", "ZZ", "Flags/Gibberish_flag.png");
    int currentIndex;

    private static LanguageOptions singleInstance = null;

    /**
     * constructor for language array
     */
    public LanguageOptions() {
        langOptions = new ArrayList<>();
        langOptions.add(english); //default language
        langOptions.add(spanish);
        langOptions.add(dutch);
        langOptions.add(gibberish);
        currentIndex = 0;
    }

    /**
     * Getter for the instance
     * @return the instance of a language
     */
    public static synchronized LanguageOptions getInstance(){
        if (singleInstance==null)
            singleInstance = new LanguageOptions();
        return singleInstance;
    }

    /**
     * The getter for the current language
     * @return the current language
     */
    public Language getCurrentLanguage() {
        return langOptions.get(currentIndex);
    }

    /**
     * The setter for the current index of the language
     * @param newIndex - the new index for the language to be set to
     */
    public void setCurrentIndex(int newIndex) {
        currentIndex = newIndex;
    }

    /**
     * The adder for the language
     * @param newLanguage - new language to be added
     * @throws IllegalArgumentException - when the argument is illegal
     */
    public void addLanguage(Language newLanguage) throws IllegalArgumentException {
        for (int i = 0; i < langOptions.size(); i++) {
            if (langOptions.get(i).getId() == newLanguage.getId()) {
                throw new IllegalArgumentException(
                        "New language ID cannot be the same as option "
                                + langOptions.get(i).toString());
                //todo - check if toString for language is implemented
            }
        }
        langOptions.add(newLanguage);
    }

    /**
     * The getter for the language options
     * @return list of languages
     */
    public ArrayList<Language> getLanguages() {
        return langOptions;
    }

    /**
     * The getter for all abbreviations of the languages
     * @return list of languages
     */
    public ArrayList<Language> getAllAbbreviations() {
        ArrayList<Language> languagesAbbrs = new ArrayList<Language>();
        for (int i = 0; i < langOptions.size(); i++) {
            languagesAbbrs.add(i, langOptions.get(i));
        }
        return languagesAbbrs;
    }

    /**
     * Getter for the language based on the abbreviation
     * @param abbr - the abbreviation of the language
     * @return a language based on these parameters
     * @throws IllegalArgumentException - when something messes up
     */
    public Language getLanguageFromAbbr(String abbr) throws IllegalArgumentException{
        for (int i = 0; i < langOptions.size(); i++) {
            if (langOptions.get(i).getAbbr().equals(abbr)){
                return langOptions.get(i);
            }
        }
        throw new IllegalArgumentException("Abbreviation "+abbr+ "not found");
    }

}
