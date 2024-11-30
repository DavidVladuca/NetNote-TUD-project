package commons;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Note> notes = new ArrayList<>();

    public String name;

    @SuppressWarnings("unused")
    User() {
        // for object mapper
    }

    /**
     * Constructor for the Class User
     * @param name - name of the User
     */
    public User(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    /**
     * Equals method for the User class.
     * Returns true if obj is also a User and has the same parameters.
     * Else returns false
     * @param obj - Object we compare with
     * @return boolean whether they are equal or not
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * hashCode method for the User Class
     * @return a hash code representation of the User Object
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * toString method for the User Class
     * @return a human friendly representation of the User Object
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}