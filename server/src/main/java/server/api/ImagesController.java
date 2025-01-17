package server.api;

import commons.Images;
import commons.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.ImageRepository;
import server.database.NoteRepository;

import java.util.List;
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
    public ResponseEntity<Images> addImageToNote(@PathVariable Long noteId,
                                                 @RequestBody Images image) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Note note = noteOptional.get();
        image.setNote(note);
        Images savedImage = imageRepository.save(image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
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
}
