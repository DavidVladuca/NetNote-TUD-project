package commons;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long collectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false) // Foreign key to Server
    @JsonBackReference
    private Server server;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String collectionTitle;

    @Column(name = "collection_path", nullable = false)
    private String collectionPath = "-";

    @Column(name = "default_collection", nullable = false)
    private boolean defaultCollection = false;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Note> notes;

    /**
     * Default constructor for Collection for JPA
     */
    public Collection() {
        this.notes = new ArrayList<>();
    }

    private long latestNoteId = -1;

    /**
     * Constructor for creating a new Collection instance.
     *
     * @param server          - The server associated with the collection.
     * @param collectionTitle - The title of the collection.
     * @param collectionPath  - The path in the URL for the collection
     * @param isDefaultCollection - The type of collection
     */
    public Collection(Server server, String collectionTitle,
                      String collectionPath, boolean isDefaultCollection) {
        this.server = server;
        this.collectionTitle = collectionTitle;
        this.collectionPath = collectionPath;
        this.notes = new ArrayList<>();
        this.defaultCollection = isDefaultCollection;
    }

    /**
     * The getter for the collection path
     *
     * @return a String representing the collection path
     */
    public String getCollectionPath() {
        return collectionPath;
    }

    /**
     * Setter for the collection path
     *
     * @param collectionPath - collection path to be set for this particular collection
     */
    public void setCollectionPath(String collectionPath) {
        this.collectionPath = collectionPath;
    }

    /**
     * Getter for the collectionId
     *
     * @return integer value of the collectionId
     */
    public long getCollectionId() {
        return collectionId;
    }

    /**
     * Setter for the collectionId
     *
     * @param collectionId - the value of the ID that we want to set
     */
    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * Getter for the Server
     *
     * @return - Server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Setter for the server
     *
     * @param server - Server provided
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Getter for the Title of the collection
     *
     * @return title of the collection
     */
    public String getCollectionTitle() {
        return collectionTitle;
    }

    /**
     * Setter for the collection title
     *
     * @param collectionTitle - title of the collection provided
     */
    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    /**
     * Getter for the Notes
     *
     * @return - Notes of the collection
     */
    public List<Note> getNotes() {
        return notes;
    }

    /**
     * Setter for the Notes
     *
     * @param notes - List of Notes provided
     */
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    /**
     * Equals method for the Collection class.
     * Returns true if obj is also a Note and has the same ID.
     * Else returns false
     *
     * @param obj - Object we compare with
     * @return boolean whether they are equal or not based on the ID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Collection that = (Collection) obj;
        return collectionId == that.collectionId;
    }

    /**
     * hashCode method for the Collection Class
     *
     * @return a hash code representation of the Collection Object
     */
    @Override
    public int hashCode() {
        return Long.hashCode(collectionId);
    }

    /**
     * toString method for the Collection Class
     *
     * @return a human friendly representation of the Collection Object
     */
    @Override
    public String toString() {
        StringBuilder notesString = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
            notesString.append(notes.get(i).toString());
            if (i < notes.size() - 1) {
                notesString.append("\n---------------------------\n");
            }
        }
        return "Collection:\n" +
                "Collection ID: " + collectionId + "\n" +
                "Server ID: " + server.getServerId() + "\n" +
                "Collection Title: " + collectionTitle + "\n" +
                "Collection Path: " + collectionPath + "\n" +
                "Default Collection: " + defaultCollection + "\n" +
                "Notes:\n\n" + (!notesString.isEmpty() ? notesString : "No Notes");
    }

    /**
     * method to add note
     *
     * @param newNote - note to be added to the collection
     */
    public void addNote(Note newNote) {
        notes.add(newNote);
        latestNoteId++;
    } //todo - make it so that you can also add notes
    //todo - add tests


    /**
     * This method gets the note by its id
     *
     * @param id - id provided
     * @return a Note object with this id
     */
    public Note getNoteByID(int id) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getNoteId() == id)
                return notes.get(i);
        }
        return null;
    }

    /**
     * This method removes a note provided
     * @param note - note to be removed
     */
    public void removeNote(Note note) {
        notes.remove(note);
    }

    /**
     * searches collections
     *
     * @param searchText - text that needs to be matched
     * @return - returns an arraylist of arraylists, where each nested arraylist's first
     * integer is the note id, and all following. Integers are indices within that note
     * of the first match in the note. Uses the note's search itself to find them. If
     * empty string, returns null. If no matches, returns a single nested arraylist
     * containing -1 (to check for no matches, getFirst().getFirst()==-1).
     */
    public ArrayList<ArrayList<Long>> getSearch(String searchText) {
        ArrayList<ArrayList<Long>> result = new ArrayList<>();
        if (searchText.isEmpty())
            return result;

        for (int i = 0; i < notes.size(); i++) {
            ArrayList<Long> noteMatch = new ArrayList<>();
            //already checked before if empty, so this will not be empty
            if (notes.get(i).getMatchIndices(searchText).getFirst() != -1) {
                noteMatch.add(notes.get(i).getNoteId());
                noteMatch.addAll(notes.get(i).getMatchIndices(searchText));
                result.add(noteMatch);
            }
        }
        if (result.isEmpty()) {
            ArrayList<Long> noteMatch = new ArrayList<>();
            noteMatch.add(-1L);
            //if no matches, returns 1 arraylist containing -1 (getFirst().getFirst()==-1)
            result.add(noteMatch);
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

    /**
     * getter for isDefaultCollection
     * @return value of isDefaultCollection
     */
    public boolean isDefaultCollection() {
        return defaultCollection;
    }

    /**
     * setter for isDefaultCollection
     * @param defaultCollection - new value of isDefaultCollection
     */
    public void setDefaultCollection(boolean defaultCollection) {
        this.defaultCollection = defaultCollection;
    }
}
