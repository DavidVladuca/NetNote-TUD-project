package commons;

import java.util.Objects;

public class Language {
    private int id; //needed for the model selection
    private String name;
    private String abbr; //abbreviation
    private String img_path;


    /**
     * Constructor
     * @param id - id number for the language
     * @param name - name (complete) of the language
     * @param abbr - two letter abbreviation of the language
     * @param img_path - path for the location of the language flag
     */
    public Language(int id, String name, String abbr, String img_path) throws IllegalArgumentException{
        if (abbr.length()!=2){
            throw new IllegalArgumentException("Abbreviation must be two characters long (use ISO 639).");
        } else if (abbr.matches("[^A-Z]")){
            throw new IllegalArgumentException("Abbreviation must only contain uppercase letters");
        }else{
            this.id = id;
            this.name = name;
            this.abbr = abbr;
            this.img_path = img_path;
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
     * getter for location of the language flag
     * @return - returns the path for the flag of the language
     */
    public String getImg_path(){
        return img_path;
    }

    /**
     * Sets a new id
     * @param new_id
     */
    public void setId(int new_id){
        this.id = new_id;
    }

    /**
     * Compares another object for equality
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if(this==other) return true;

        if(other==null||getClass()!=other.getClass()) return false;
        Language that = (Language) other;
        return this.id == that.id &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.abbr, that.abbr) &&
                Objects.equals(this.img_path, that.img_path);
    }

    /**
     * Returns a hashcode value for the instance
     * @return int hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, abbr, img_path);
    }

    /**
     * Returns a humanreadable version of the object as a string
     * @return
     */
    @Override
    public String toString() {
        return "Language{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", abbr='" + abbr + '\'' +
                ", img_path='" + img_path + '\'' +
                '}';
    }
}
