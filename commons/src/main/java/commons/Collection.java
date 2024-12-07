package commons;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Collection {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private long collectionId;
    private long latestNoteId = -1;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false) // Foreign key to Server
    @JsonBackReference
    private Server server;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String collectionTitle;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Note> notes;

    // Default constructor for JPA
    public Collection() {
        this.notes = new ArrayList<>();
    }

    /**
     * Constructor for creating a new Collection instance.
     *
     * @param server - The server associated with the collection.
     * @param collectionTitle - The title of the collection.
     */
    public Collection(Server server, String collectionTitle) {
        this.server = server;
        this.collectionTitle = collectionTitle;
        this.notes = new ArrayList<>();
    }

    /**
     * Getter for the collectionId
     * @return integer value of the collectionId
     */
    public long getCollectionId() {
        return collectionId;
    }

    /**
     * Setter for the collectionId
     * @param collectionId - the value of the ID that we want to set
     */
    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * Getter for the Server
     * @return - Server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Setter for the server
     * @param server - Server provided
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Getter for the Title of the collection
     * @return title of the collection
     */
    public String getCollectionTitle() {
        return collectionTitle;
    }

    /**
     * Setter for the collection title
     * @param collectionTitle - title of the collection provided
     */
    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    /**
     * Getter for the Notes
     * @return - Notes of the collection
     */
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Setter for the Notes
     * @param notes - List of Notes provided
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    /**
     * Equals method for the Collection class.
     * Returns true if obj is also a Note and has the same ID.
     * Else returns false
     * @param obj - Object we compare with
     * @return boolean whether they are equal or not based on the ID
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || getClass() != obj.getClass())
            return false;
        Collection that = (Collection) obj;
        return collectionId == that.collectionId;
    }

    /**
     * hashCode method for the Collection Class
     * @return a hash code representation of the Collection Object
     */
    @Override
    public int hashCode() {
        return Long.hashCode(collectionId);
    }

    /**
     * toString method for the Collection Class
     * @return a human friendly representation of the Collection Object
     */
    @Override
    public String toString() {
        StringBuilder notesString = new StringBuilder();
        for(int i = 0; i < notes.size(); i++) {
            notesString.append(notes.get(i).toString());
            if(i < notes.size() - 1) {
                notesString.append("\n---------------------------\n");
            }
        }
        return "Collection:\n" +
                "Collection ID: " + collectionId + "\n" +
                "Server ID: " + server.getServerId() + "\n" +
                "Collection Title: " + collectionTitle + "\n" +
                "Notes:\n\n" + (!notesString.isEmpty() ? notesString : "No Notes");
    }

    /**
     * method to add note
     * @param new_note - note to be added to the collection
     */
    public void addNote(Note new_note){
        notes.add(new_note);
        latestNoteId++;
    } //todo - make it so that you can also add notes
    //todo - add tests
    /**
     * searches collections
     * @param search_text - text that needs to be matched
     * @return - returns an arraylist of arraylists, where each nested arraylist's first integer is the note id, and all following
     * integers are indices within that note of the first match in the note. Uses the note's search itself to find them. If
     * empty string, returns null. If no matches, returns a single nested arraylist containing -1 (to check for no matches,
     * getFirst().getFirst()==-1).
     */
    public ArrayList<ArrayList<Long>>  getSearch(String search_text){
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        if (search_text.isEmpty())
            return result;

        for (int i=0; i<notes.size(); i++){
            ArrayList<Long> note_match = new ArrayList<>();
            if (notes.get(i).getMatchIndices(search_text).getFirst()!=-1){ //already checked before if empty, so this will not be empty
                note_match.add(notes.get(i).getNoteId());
                note_match.addAll(notes.get(i).getMatchIndices(search_text));
                result.add(note_match);
            }
        }
        if (result.isEmpty()){
            ArrayList<Long> note_match = new ArrayList<>();
            note_match.add(-1L);
            result.add(note_match); //if no matches, returns 1 arraylist containing -1 (getFirst().getFirst()==-1)
        }
        return result;
    }

    /**
     * getter for latest note id
     * @return returns note id of the previous note
     */
    public long getLatestNoteId() {
        return latestNoteId;
    }
}
