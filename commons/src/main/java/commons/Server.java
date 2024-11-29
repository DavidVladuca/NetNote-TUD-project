package commons;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int serverId;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Collection> collections;

    // Default constructor for JPA
    protected Server() {
        this.collections = new ArrayList<>();
    }

    /**
     * Getter for the serverId
     * @return integer value of the serverId
     */
    public int getServerId() {
        return serverId;
    }

    /**
     * Setter for the serverId
     * @param serverId - the value of the ID that we want to set
     */
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    /**
     * This method adds a collection to the server
     * @param collection - collection that we are adding
     */
    public void addCollection(Collection collection) {
        this.collections.add(collection);
    }

    /**
     * Equals method for the Server class.
     * Returns true if obj is also a Server and has the same serverId.
     *
     * @param obj - Object we compare with.
     * @return boolean whether they are equal or not based on the serverId.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Server server = (Server) obj;
        return serverId == server.serverId;
    }

    /**
     * hashCode method for the Server Class.
     * @return a hash code representation of the Server Object.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(serverId);
    }

    /**
     * toString method for the Server Class.
     * @return a human-friendly representation of the Server Object.
     */
    @Override
    public String toString() {
        StringBuilder collectionsString = new StringBuilder();
        for (int i = 0; i < collections.size(); i++) {
            collectionsString.append(collections.get(i).toString());
            if (i < collections.size() - 1) {
                collectionsString.append("\n#########################\n");
            }
        }
        return "Server:\n" +
                "Server ID: " + serverId + "\n" +
                "Collections:\n\n" + (!collectionsString.isEmpty() ? collectionsString : "No Collections");
    }
}
