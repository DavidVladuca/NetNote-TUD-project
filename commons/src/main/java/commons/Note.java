package commons;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    @ManyToOne
    @JoinColumn(name = "user_id") // Adjust the column name as needed
    private User user;
    public String contents;

    @SuppressWarnings("unused")
    private Note() {
        // for object mappers
    }

    /**
     * Constructor for the Note Class.
     * @param contents - String representing the content of the note.
     */
    public Note(String contents) {
        this.contents = contents;
    }

    /**
     * Equals method for the Note class.
     * Returns true if obj is also a Note and has the same parameters.
     * Else returns false
     * @param obj - Object we compare with
     * @return boolean whether they are equal or not
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * hashCode method for the Note Class
     * @return a hash code representation of the Note Object
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * toString method for the Note Class
     * @return a human friendly representation of the Note Object
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
