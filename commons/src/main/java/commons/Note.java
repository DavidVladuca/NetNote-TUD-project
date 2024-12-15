package commons;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long noteId; //todo - this does not work well all notes have id 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false) // Ensure that the title is mandatory in the database
    @JsonBackReference
    private Collection collection;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String title;

    @Column(nullable = false) // Ensure that the title is mandatory in the database
    private String body;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "note_tag",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Protected no-arg constructor for JPA and object mappers
    public Note() {}

    /**
     * Constructor for the Note class.
     * @param title - Title of the note.
     * @param body - Content/body of the note.
     * @param collection - The collection the note is part of.
     */
    public Note(String title, String body, Collection collection) {
        this.title = title;
        this.body = body;
        this.collection = collection;
        this.tags = new HashSet<>(); // declare it empty for now
    }

    /**
     * Getter for the noteId
     * @return integer value of the noteId
     */
    public long getNoteId() {
        return noteId;
    }

    /**
     * Setter for the noteId
     * @param noteId - the value of the ID that we want to set
     */
    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    /**
     * Getter for the title
     * @return String value of the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for the title
     * @param title - the value of the title that we want to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Getter for the body
     * @return String value of the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Setter for the body
     * @param body - the value of the body that we want to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Getter for the collection
     * @return - Collection
     */
    public Collection getCollection() {
        return this.collection;
    }

    /**
     * Setter for the collection
     * @param collection
     */
    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    /**
     * Getter for the Tags
     * @return - the list of Tags representing the tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Setter for the tags
     * @param tags - list of Tags representing the tags
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
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
        return Long.hashCode(noteId);
    }

    /**
     * toString method for the Note Class
     * @return a human friendly representation of the Note Object
     */
    @Override
    public String toString() {
        StringBuilder tagsString = new StringBuilder();
        for (Tag tag : tags) {
            tagsString.append(tag.getName());
            tagsString.append(", ");
        }
        // Remove the last comma and space, if any tags were appended
        if (!tagsString.isEmpty()) {
            tagsString.setLength(tagsString.length() - 2);
        }
        return "Note:\n" +
                "Note ID: " + noteId + "\n" +
                "Collection ID: " + (collection != null ? collection.getCollectionId() : "No Collection") + "\n" +
                "Title: " + title + "\n" +
                "Body:\n" + body + "\n" +
                "Tags: " + tagsString + "\n";
    }

    /**
     * gets indices for all matches for a particular search text
     * @param search_text - inputted text by user
     * @return - returns ArrayList of the starting index of all matches in the order in which they appear in the text; -1 if there are no matches, and empty array if no text has been introduced
     */
    public ArrayList<Long> getMatchIndices(String search_text) {
        String total_content = title+body;
        ArrayList<Long> matches = new ArrayList<Long>();
        if (search_text.isEmpty()) //before the user starts to write, the method will (likely) still be called todo - check if it is
            return matches;
        for (int i = 0; i < total_content.length() - search_text.length()+1; i++) {
            if (total_content.startsWith(search_text, i))
                matches.add((long) i);
        }
        if (matches.isEmpty())
            matches.add(-1L);
        return matches;
    }
}
