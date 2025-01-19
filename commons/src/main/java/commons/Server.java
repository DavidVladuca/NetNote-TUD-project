package commons;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Server {

    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private long serverId;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Collection> collections;

    private String URL = "http://localhost:8080";

    /**
     * The default constructor for JPA
     */
    public Server() {
        this.collections = new ArrayList<>();
    }

    /**
     * Getter for the serverId
     * @return integer value of the serverId
     */
    public long getServerId() {
        return serverId;
    }

    /**
     * The getter for the URL
     * @return the URL of the server
     */
    public String getURL() {
        return URL;
    }

    /**
     * The setter for the URL
     * @param url - the url to be replaced
     */
    public void setURL(String url) {
        this.URL = url;
    }

    /**
     * Setter for the serverId
     * @param serverId - the value of the ID that we want to set
     */
    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    /**
     * The getter for the collections of the server
     * @return the list of collections associated with the server
     */
    public List<Collection> getCollections() {
        return collections;
    }

    /**
     * The setter for the collections
     * @param collections - the collections to be set
     */
    public void setCollections(List<Collection> collections) {
        this.collections = collections;
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
        return Long.hashCode(serverId);
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
                "Collections:\n\n" + (!collectionsString.isEmpty() ?
                collectionsString : "No Collections");
    }
}
