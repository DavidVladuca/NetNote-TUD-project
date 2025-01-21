package commons;

import jakarta.persistence.*;

import java.util.Arrays;


@Entity
public class Images {
    private String mimeType;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String url;

    @Lob
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    /**
     * Default Constructor
     */
    public Images() {};

    /**
     * Constructor with parameters
     * @param id
     * @param name
     * @param data
     * @param note
     */
    public Images(Long id, String name, byte[] data, Note note) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.note = note;
    }

    /**
     * Returns Image id
     * @return Long id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id of the image
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the Image name
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the image
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the data of the image
     * @return byte[] data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the data of the image
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the parent note of the image
     * @return Note
     */
    public Note getNote() {
        return note;
    }

    /**
     * Sets the parent note of the image
     * @param note
     */
    public void setNote(Note note) {
        this.note = note;
    }

    /**
     * Gets the mimetype of an image
     * @return mimetype as a string
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mimetype of an image
     * @param mimeType
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Equals method for images
     * @param o
     * @return boolean for equality
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Images image = (Images) o;
        return this.id == image.getId();
    }

    /**
     * Hashcode method for images
     * @return integer hash value
     */
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    /**
     * To String method for images
     * @return
     */
    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", data=" + Arrays.toString(data) +
                ", note=" + note +
                '}';
    }

    /**
     * Sets the url of a file
     * @param fileUrl
     */
    public void setFileUrl(String fileUrl) {
        this.url = fileUrl;
    }

    /**
     * Retrieves the url of a file
     * @return String of url
     */
    public String getFileUrl() {
        return url;
    }
}
