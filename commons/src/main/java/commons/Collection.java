package commons;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int collectionId;

    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false) // Foreign key to Server
    private Server server;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String collectionTitle;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
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
    public int getCollectionId() {
        return collectionId;
    }

    /**
     * Setter for the collectionId
     * @param collectionId - the value of the ID that we want to set
     */
    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
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
        return Integer.hashCode(collectionId);
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
}
