package commons;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "tagId",
        scope = Tag.class
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long tagId;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Note> notes = new HashSet<>();

    public Tag() {}

    /**
     * Constructor for creating a new Tag instance.
     * @param name - The unique name of the tag.
     */
    public Tag(String name) {
        this.name = name;
    }

    /**
     * Getter for the tag ID.
     * @return long value of the tag ID.
     */
    public long getTagId() {
        return tagId;
    }

    /**
     * Setter for the tag ID.
     * @param tagId - The value to set as the tag ID.
     */
    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    /**
     * Getter for the tag name.
     * @return String representing the tag's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the tag name.
     * @param name - The unique name of the tag to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the Notes associated with this tag.
     * @return A Set of Notes that are associated with this tag.
     */
    public Set<Note> getNotes() {
        return notes;
    }

    /**
     * Setter for the Notes associated with this tag.
     * @param notes - A Set of Notes to associate with this tag.
     */
    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    /**
     * Equals method for the Tag class.
     * Returns true if obj is also a Tag and has the same ID.
     * Else returns false
     * @param obj - Object we compare with
     * @return boolean whether they are equal or not based on the ID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return Objects.equals(name, tag.name);
    }

    /**
     * HashCode method for the Tag class.
     * @return An integer hash code for the Tag object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * toString method for the Tag Class
     * @return a human friendly representation of the Tag Object
     */
    @Override
    public String toString() {
        return "(ID - " + tagId +
                ", Name - " + name + ")";
    }
}
