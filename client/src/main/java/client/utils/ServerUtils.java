
/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import commons.Images;
import commons.Note;
import commons.Tag;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";

    /**
     * This method validates the title with server if it is or
     * is not duplicate.
     *
     * @param collectionId - id of the collection that is the note
     *                     associated with
     * @param newTitle     - the title to be checked
     * @return true if it is a duplicate, false if it is not
     * @throws IOException when it returns something else then 200/409 code
     */
    public boolean validateTitleWithServer(
            final Long collectionId, final String newTitle)
            throws IOException {
        String endpoint = "api/notes/validate-title";

        Response response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)  // Endpoint path
                .queryParam("collectionId", collectionId)
                .queryParam("title", newTitle)
                .request()
                .get();

        if (response.getStatus() == 409) {
            System.out.println("Duplicate title detected: " + newTitle);
            return true;
        } else if (response.getStatus() == 200) {
            System.out.println("Title is unique: " + newTitle);
            return false;
        } else {
            throw new IOException("Error: " + response.getStatus());
        }
    }

    /**
     * This method ensures that the collection is synced with the server
     * @param collection - the collection to be synced
     * @return the synced collection
     */
    public Collection syncCollectionWithServer(Collection collection) {
        try {
            // Serializing the collection to JSON
            String json = new ObjectMapper().writeValueAsString(collection);
            String endpoint = "api/collections/update/"
                    + collection.getCollectionId();

            // Creating a PUT request to update the specific collection
            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody);

            if (response.getStatus() == 200) {
                // Parsing the server's response into a Collection object
                String updatedCollectionJson = response.readEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(updatedCollectionJson, Collection.class);
            } else {
                System.err.println("Failed to sync collection. Status code: "
                        + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error syncing collection with the server: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * This method loads the collections from the server when starting the client
     * @return a list of collections on the server
     */
    public List<Collection> loadCollectionsFromServer() {
        String endpoint = "api/collections/fetch";
        try {
            // Fetch collections from the server
            var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                // Parse the JSON response into a List of Collection objects
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Collection> fetchedCollections = mapper.readValue(
                        json,
                        mapper.getTypeFactory()
                                .constructCollectionType(List.class, Collection.class)
                );

                System.out.println("Collections loaded successfully from the server.");
                return fetchedCollections; // Return the fetched collections
            } else {
                System.err.println("Failed to fetch collections. Error code: "
                        + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading the collections: " + e.getMessage());
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * This method loads the Tags from the server on the start-up
     * @return a list of Tags
     */
    public List<Tag> loadTagsFromServer() {
        String endpoint = "api/tags";
        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Tag> fetchedTags = mapper.readValue(
                        json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Tag.class)
                );
                System.out.println("Tags loaded successfully from the server.");
                return fetchedTags;
            } else {
                System.err.println("Failed to fetch tags. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading tags from the server: " + e.getMessage());
            e.printStackTrace();
        }

        return Collections.emptyList(); // Return an empty list if an error occurs
    }

    /**
     * This method saves the Tag to Server
     * @param tag - the tag to be saved
     * @throws IOException - Exception
     */
    public void saveTagToServer(final Tag tag) throws IOException {
        String json = new ObjectMapper().writeValueAsString(tag);
        String endpoint = "api/tags/create";
        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(json, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new IOException("Failed to save tag. Status: " + response.getStatus());
            }
        }
    }

    /**
     * This method synces the tags in the note with the server
     * @param note - the note with the tags
     */
    public void syncNoteTagsWithServer(final Note note) {
        try {
            // Serialize the tags to JSON
            ObjectMapper mapper = new ObjectMapper();
            Set<String> tagNames = note.getTags()
                    .stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());
            String json = mapper.writeValueAsString(tagNames);
            String endpoint = "api/notes/" + note.getNoteId() + "/tags";
            // Send the PUT request to the server
            try (Response response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(json, MediaType.APPLICATION_JSON))) {

                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    System.err.println("Failed to sync tags with server. Status: "
                            + response.getStatus());
                } else {
                    System.out.println("Tags synced successfully for note: " + note.getNoteId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing tags: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This method loads the notes from the server during the start-up of the client
     * @return a list of notes loaded
     */
    public List<Note> loadNotesFromServer() {
        String endpoint = "api/notes/fetch";
        try {
            // Fetch notes from the server
            var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                // Parse the JSON response into a List of Note objects
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(
                        json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Note.class)
                );
            } else {
                System.err.println("Failed to fetch notes. Error code: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading notes from the server: " + e.getMessage());
            e.printStackTrace();
        }

        return Collections.emptyList(); // Return an empty list if an error occurs
    }

    /**
     * This method synces the note with the server
     * @param note - the note to be synced
     * @return true if server returns 200, false otherwise
     */
    public boolean syncNoteWithServer(final Note note) {
        String endpoint = "api/notes/update";
        try {
            String json = new ObjectMapper().writeValueAsString(note);
            System.out.println("Serialized JSON: " + json);  // For testing

            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            try (var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody)) {

                System.out.println("Response Status: " + response.getStatus());
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    return true; // Sync successful
                } else {
                    System.err.println(
                            "Failed to update note on server. Status code: "
                                    + response.getStatus());
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing note with server: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method saves the note to server
     * @param note - the note to be saved
     * @return a Note
     * @throws IOException - Exception
     */
    public Note saveNoteToServer(final Note note) throws IOException {
        String endpoint = "api/notes/create";
        String json = new ObjectMapper().writeValueAsString(note);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody)) {

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                Note addedNote = response.readEntity(Note.class);
                System.out.println("Note saved successfully. ID: " + addedNote.getNoteId());
                return addedNote;
            } else {
                throw new IOException("Failed to save note. Server returned status: "
                        + response.getStatus());
            }
        }
    }

    /**
     * This method fetches the note by its id from the database
     * @param noteId - the id
     * @return a Note fetched by this id
     */
    public Note fetchNoteById(final long noteId) {
        String endpoint = "api/notes/" + noteId;

        try {
            var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, Note.class);
            } else {
                System.err.println(
                        "Failed to fetch note with ID "
                                + noteId
                                + ". Status code: "
                                + response.getStatus()
                );
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching note with ID "
                    + noteId + ": "
                    + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method saves the image to the server
     * @param image - the image to be saved
     * @param noteId - the note id the image is associated with
     * @return an Image
     * @throws IOException - Exception
     */
    public Images saveImageToServer(final Images image, final long noteId) throws IOException {
        // Convert the image object to JSON
        String json = new ObjectMapper().writeValueAsString(image);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

        // Construct the URL with the noteId
        String endpoint = "api/images/" + noteId + "/addImage";

        // Send a POST request to the server
        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody)) {

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                System.out.println("Image saved successfully");
                return response.readEntity(Images.class);
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new IOException("Server returned 404: Note not found");
            } else {
                throw new IOException("Server returned status: " + response.getStatus());
            }
        }
    }

    /**
     * This method fetches the images for a certain note
     * @param noteId - the note id provided
     * @return a list of images in the note provided
     */
    public List<Images> fetchImagesForNote(final long noteId) {
        String endpoint = "api/images/" + noteId + "/allImages";

        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                // Parse the JSON response
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Images.class));
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                System.err.println("No images found for note: " + noteId);
                return List.of(); // Return empty list
            } else {
                throw new IOException("Failed to fetch images. Server returned status: "
                        + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error fetching images: " + e.getMessage());
            return List.of(); // Return empty list in case of failure
        }
    }

    /**
     * This method deletes the note by ID
     * @param noteId - the note id provided
     * @return - true if the note was deleted successfully, false otherwise
     */
    public boolean deleteRequest(final long noteId) {
        if (noteId <= 0) {
            throw new IllegalArgumentException("Invalid note ID provided.");
        }

        String endpoint = "api/notes/delete/" + noteId;

        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request()
                .delete()) {

            if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                System.out.println("Note successfully deleted.");
                return true;
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                System.err.println("Note not found.");
            } else {
                System.err.println("Failed to delete note. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error deleting note: " + e.getMessage());
        }

        return false;
    }

    /**
     * This method adds a note to the server - for command pattern
     * @param note - note provided
     * @return true if the add was successful, false otherwise
     */
    public boolean addNoteToServer(final Note note) {
        String endpoint = "api/notes/create";
        try {
            var json = new ObjectMapper().writeValueAsString(note);
            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

            try (var response = ClientBuilder.newClient()
                    .target(SERVER)
                    .path(endpoint)
                    .request(MediaType.APPLICATION_JSON)
                    .post(requestBody)) {

                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    System.out.println("Note added successfully: " + response.toString());
                    return true;
                } else {
                    System.err.println("Failed to add note. Status: " + response.getStatus());
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding note to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * This method says if the server is or is not available
     *
     * @return the boolean that is true if the server is available,
     * and is false if the server is not
     */
    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig()) //
                    .target(SERVER) //
                    .request(APPLICATION_JSON) //
                    .get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the metadata of an image on the server.
     *
     * @param image The image to be updated.
     * @return The updated image object if the operation is successful, or null otherwise.
     * @throws IOException If the server returns an unexpected response.
     */
    public Images updateImageOnServer(final Images image) throws IOException {
        if (image == null || image.getId() == null) {
            throw new IllegalArgumentException("Image or image ID cannot be null.");
        }

        String endpoint = "api/images/" + image.getId();
        String json = new ObjectMapper().writeValueAsString(image); // Convert image to JSON
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

        try (var response = ClientBuilder.newClient()
                .target(SERVER)
                .path(endpoint)
                .request(MediaType.APPLICATION_JSON)
                .put(requestBody)) {

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                System.out.println("Image updated successfully: " + image.getName());
                return response.readEntity(Images.class); // Return the updated image
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new IOException("Image not found. Server returned 404.");
            } else {
                throw new IOException("Failed to update image. Server returned status: " + response.getStatus());
            }
        }
    }

}