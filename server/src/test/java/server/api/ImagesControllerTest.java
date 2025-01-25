package server.api;

import commons.Images;
import commons.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.database.ImageRepository;
import server.database.NoteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ImagesControllerTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private ImagesController imagesController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddImageToNote_Success() {
        Note note = new Note();
        note.setTitle("Sample Note");
        note.setNoteId(1L);

        Images image = new Images();
        image.setName("test-image.jpg");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(imageRepository.save(any(Images.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, String>> response = imagesController.addImageToNote(1L, image);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("http://server/api/images/files/notes/Sample%20Note/test-image.jpg", response.getBody().get("fileUrl"));

        verify(noteRepository, times(1)).findById(1L);
        verify(imageRepository, times(1)).save(any(Images.class));
    }

    @Test
    void testAddImageToNote_NoteNotFound() {
        when(noteRepository.findById(1L)).thenReturn(Optional.empty());

        Images image = new Images();
        ResponseEntity<Map<String, String>> response = imagesController.addImageToNote(1L, image);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noteRepository, times(1)).findById(1L);
        verify(imageRepository, never()).save(any(Images.class));
    }

    @Test
    void testGetImagesForNote_Success() {
        List<Images> imagesList = new ArrayList<>();
        imagesList.add(new Images());

        when(noteRepository.existsById(1L)).thenReturn(true);
        when(imageRepository.findAllByNote_NoteId(1L)).thenReturn(imagesList);

        ResponseEntity<List<Images>> response = imagesController.getImagesForNote(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(noteRepository, times(1)).existsById(1L);
        verify(imageRepository, times(1)).findAllByNote_NoteId(1L);
    }

    @Test
    void testGetImagesForNote_NoteNotFound() {
        when(noteRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<List<Images>> response = imagesController.getImagesForNote(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noteRepository, times(1)).existsById(1L);
        verify(imageRepository, never()).findAllByNote_NoteId(1L);
    }

    @Test
    void testGetImageById_Success() {
        Images image = new Images();
        image.setId(1L);

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));

        ResponseEntity<Images> response = imagesController.getImageById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(image, response.getBody());
        verify(imageRepository, times(1)).findById(1L);
    }

    @Test
    void testGetImageById_NotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Images> response = imagesController.getImageById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(imageRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteImage_Success() {
        when(imageRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = imagesController.deleteImage(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(imageRepository, times(1)).existsById(1L);
        verify(imageRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteImage_NotFound() {
        when(imageRepository.existsById(1L)).thenReturn(false);

        ResponseEntity<Void> response = imagesController.deleteImage(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(imageRepository, times(1)).existsById(1L);
        verify(imageRepository, never()).deleteById(anyLong());
    }
}
