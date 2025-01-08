package client.utils;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NoteValidator {

    /**
     * This method validates the title with server if it is or is not duplicate
     * @param collectionId - id of the collection that is the note associated with
     * @param newTitle - the title to be checked
     * @return true if it is a duplicate, false if it is not
     * @throws IOException when it returns something else then 200/409 code
     */
    public boolean validateTitleWithServer(Long collectionId, String newTitle) throws IOException {
        String endpoint = "http://localhost:8080/api/notes/validate-title";
        String query = "?title=" + URLEncoder.encode(newTitle, StandardCharsets.UTF_8);

        Response response = ClientBuilder.newClient()
                .target(endpoint + query)
                .request()
                .get();

        if (response.getStatus() == 409) {
            return true;
        } else if (response.getStatus() == 200) {
            return false;
        } else {
            throw new IOException("Server error: " + response.getStatus());
        }
    }
}