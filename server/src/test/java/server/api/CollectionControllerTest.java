package server.api;

import commons.Collection;
import commons.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.database.CollectionRepository;
import server.database.ServerRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CollectionControllerTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private ServerRepository serverRepository;

    @InjectMocks
    private CollectionController collectionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCollection_Success() {
        Server server = new Server();
        server.setServerId(0L);
        Collection collection = new Collection();
        collection.setServer(server);

        when(serverRepository.findById(0L)).thenReturn(Optional.of(server));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        ResponseEntity<Collection> response = collectionController.createCollection(collection);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void createCollection_DefaultServer() {
        Collection collection = new Collection();
        Server defaultServer = new Server();
        defaultServer.setServerId(0L);

        when(serverRepository.findById(0L)).thenReturn(Optional.empty());
        when(serverRepository.save(any(Server.class))).thenReturn(defaultServer);
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        ResponseEntity<Collection> response = collectionController.createCollection(collection);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(serverRepository, times(1)).save(defaultServer);
        verify(collectionRepository, times(1)).save(collection);
    }

    @Test
    void updateCollection_Success() {
        Long id = 1L;
        Collection existingCollection = new Collection();
        existingCollection.setCollectionId(id);
        existingCollection.setCollectionTitle("Old Title");

        Collection updatedCollection = new Collection();
        updatedCollection.setCollectionTitle("New Title");

        when(collectionRepository.findById(id)).thenReturn(Optional.of(existingCollection));
        when(collectionRepository.save(any(Collection.class))).thenReturn(existingCollection);

        ResponseEntity<Collection> response = collectionController.updateCollection(id, updatedCollection);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Title", response.getBody().getCollectionTitle());
        verify(collectionRepository, times(1)).save(existingCollection);
    }

    @Test
    void updateCollection_NotFound() {
        Long id = 1L;
        Collection updatedCollection = new Collection();

        when(collectionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            collectionController.updateCollection(id, updatedCollection);
        });
    }

    @Test
    void getAllCollections_Success() {
        Collection collection1 = new Collection();
        Collection collection2 = new Collection();

        when(collectionRepository.findAll()).thenReturn(List.of(collection1, collection2));

        List<Collection> collections = collectionController.getAllCollections();

        assertEquals(2, collections.size());
        verify(collectionRepository, times(1)).findAll();
    }

    @Test
    void deleteCollection_Success() {
        Long id = 1L;

        when(collectionRepository.existsById(id)).thenReturn(true);

        ResponseEntity<Void> response = collectionController.deleteCollection(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(collectionRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteCollection_NotFound() {
        Long id = 1L;

        when(collectionRepository.existsById(id)).thenReturn(false);

        ResponseEntity<Void> response = collectionController.deleteCollection(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(collectionRepository, never()).deleteById(id);
    }

    @Test
    void deleteCollection_InvalidId() {
        Long id = -1L;

        ResponseEntity<Void> response = collectionController.deleteCollection(id);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(collectionRepository, never()).deleteById(id);
    }
}