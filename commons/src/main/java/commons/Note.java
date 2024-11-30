package commons;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int noteId;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    private Collection collection;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    public
    String title;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String body;

    @ElementCollection
    private List<String> tags;

    // Protected no-arg constructor for JPA and object mappers
    protected Note() {}

    /**
     * Constructor for the Note class.
     * @param title - Title of the note.
     * @param body - Content/body of the note.
     * @param collection - The collection the note is part of.
     */
    public Note(String title, String body, Collection collection, User user) {
        this.title = title;
        this.body = body;
        this.collection = collection;
        this.user = user;
        this.tags = new ArrayList<>(); // declare it empty for now
    }

    /**
     * Getter for the noteId
     * @return integer value of the noteId
     */
    public int getNoteId() {
        return noteId;
    }

    /**
     * Setter for the noteId
     * @param noteId - the value of the ID that we want to set
     */
    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    /**
     * Equals method for the Note class.
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
        Note note = (Note) obj;
        return noteId == note.noteId;
    }

    /**
     * hashCode method for the Note Class
     * @return a hash code representation of the Note Object
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(noteId);
    }

    /**
     * toString method for the Note Class
     * @return a human friendly representation of the Note Object
     */
    @Override
    public String toString() {
        StringBuilder tagsString = new StringBuilder();
        for(int i = 0; i < tags.size(); i++) {
            tagsString.append(tags.get(i));
            if(i < tags.size() - 1)
                tagsString.append(", ");
        }
        return "Note:\n" +
                "Note ID: " + noteId + "\n" +
                "Collection ID: " + collection.getCollectionId() + "\n" +
                "Title: " + title + "\n" +
                "Body:\n" + body + "\n" +
                "Tags: " + tagsString + "\n";
    }
}
