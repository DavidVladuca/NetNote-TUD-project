package commons;

public class Language {
    private int id; //needed for the model selection
    private String name;
    private String abbr; //abbreviation
    private String screen_path;

    /**
     * Constructor
     * @param id - id number for the language
     * @param name - name (complete) of the language
     * @param abbr - two letter abbreviation of the language
     * @param screen_path - path for the location of the main screen for that language
     */
    public Language(int id, String name, String abbr, String screen_path) throws IllegalArgumentException{
        if (abbr.length()!=2){
            throw new IllegalArgumentException("Abbreviation must be two characters long (use ISO 639).");
        } else if (abbr.matches("[^A-Z]")){
            throw new IllegalArgumentException("Abbreviation must only contain uppercase letters");
        }else{
            this.id = id;
            this.name = name;
            this.abbr = abbr;
            this.screen_path = screen_path;
        }

    }

    /**
     * getter for language id
     * @return - returns the id number of the language
     */
    public int getId(){
        return id;
    }

    /**
     * getter for language name
     * @return - returns the name of the language
     */
    public String getName(){
        return name;
    }

    /**
     * getter for language name abbreviation
     * @return - returns the abbreviation of the language's name
     */
    public String getAbbr(){
        return abbr;
    }

    /**
     * getter for location of the language main screen
     * @return - returns the path for the main screen of the language
     */
    public String getScreen_path(){
        return screen_path;
    }

    //todo - add javadoc
    public void setId(int new_id){
        this.id = new_id;
    }


    //todo - add equals, hash, and toString methods and tests

}
