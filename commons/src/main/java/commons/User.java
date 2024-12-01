package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    public List<Note> notes;

    public String name;


    protected User() {
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
     * toString method for the User Class
     *
     * @return a human friendly representation of the User Object
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", notes=" + notes +
                ", name='" + name + '\'' +
                '}';
    }
}