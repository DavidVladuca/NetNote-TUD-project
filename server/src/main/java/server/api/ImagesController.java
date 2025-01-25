package server.api;

import commons.Images;
import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.ImageRepository;
import server.database.NoteRepository;

import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/images")
public class ImagesController {
    private final ImageRepository imageRepository;
    private final NoteRepository noteRepository;

    /**
     * Constructor for ImageController with injected repositories
     * @param imageRepository - ImageRepository
     * @param noteRepository - NoteRepository
     */
    @Autowired
    public ImagesController(ImageRepository imageRepository, NoteRepository noteRepository) {
        this.imageRepository = imageRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Endpoint to add an image to a specific note
     * @param noteId - ID of the note
     * @param image - Image object
     * @return - ResponseEntity with the saved image
     */
    @PostMapping("/{noteId}/addImage")
    public ResponseEntity<Map<String, String>> addImageToNote(@PathVariable Long noteId,
                                                              @RequestBody Images image) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Note note = noteOptional.get();
        image.setNote(note);
        image.setMimeType(determineMimeType(image.getName())); // Set MIME type
        Images savedImage = imageRepository.save(image);

        String encodedTitle = URLEncoder.encode(note.getTitle(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String encodedName = URLEncoder.encode(savedImage.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String fileUrl = String.format("http://server/api/images/files/notes/%s/%s", encodedTitle, encodedName);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("fileUrl", fileUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody); // Return JSON
    }



    /**
     * Endpoint to retrieve all images for a specific note
     * @param noteId - ID of the note
     * @return - List of images associated with the note
     */
    @GetMapping("/{noteId}/allImages")
    public ResponseEntity<List<Images>> getImagesForNote(@PathVariable Long noteId) {
        if (!noteRepository.existsById(noteId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Images> images = imageRepository.findAllByNote_NoteId(noteId);
        return ResponseEntity.ok(images);
    }

    /**
     * Endpoint to retrieve a specific image by ID
     * @param id - ID of the image
     * @return - The image object
     */
    @GetMapping("/{id}")
    public ResponseEntity<Images> getImageById(@PathVariable Long id) {
        Optional<Images> imageOptional = imageRepository.findById(id);
        if (imageOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(imageOptional.get());
    }

    /**
     * Endpoint to update an existing image's metadata
     * @param id - ID of the image
     * @param updatedImage - Updated image object
     * @return - The updated image object
     */
    @PutMapping("/{id}")
    public ResponseEntity<Images> updateImage(@PathVariable Long id,
                                              @RequestBody Images updatedImage) {
        Optional<Images> imageOptional = imageRepository.findById(id);
        if (imageOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Images existingImage = imageOptional.get();
        existingImage.setName(updatedImage.getName());
        existingImage.setData(updatedImage.getData());
        Images savedImage = imageRepository.save(existingImage);

        return ResponseEntity.ok(savedImage);
    }

    /**
     * Endpoint to delete an image by ID
     * @param id - ID of the image
     * @return - ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        if (!imageRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        imageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Displays the selected image on an url
     * @param noteTitle
     * @param fileName
     * @return image view on localhost:8080
     */
    @GetMapping("/files/notes/{noteTitle}/{fileName}")
    public ResponseEntity<byte[]> serveImage(@PathVariable String noteTitle,
                                             @PathVariable String fileName) {
        List<Images> images = imageRepository.findAll();
        for (Images image : images) {
            if (image.getNote().getTitle().equals(noteTitle) && image.getName().equals(fileName)) {
                String mimeType = determineMimeType(fileName);
                return ResponseEntity.ok()
                        .header("Content-Type", mimeType)
                        .header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                        .body(image.getData());
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private String determineMimeType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        return mimeType != null ? mimeType : "application/octet-stream";
    }

}
