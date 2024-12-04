//package commons;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class UserTest {
//    private User user1;
//    private User user2;
//    private User differentUser;
//
//    @BeforeEach
//    public void setUp() {
//        user1 = new User("Greg");
//        user2 = new User("Greg");
//        differentUser = new User("John");
//    }
//
//    @Test
//    public void testConstructor() {
//        assertEquals("Greg", user1.name);
//    }
//
//    @Test
//    public void testEquals() {
//        assertTrue(user1.equals(user2));
//    }
//
//    @Test
//    public void notEquals() {
//        assertFalse(user1.equals(differentUser));
//    }
//
//    @Test
//    public void testHashCode() {
//        assertEquals(user1.hashCode(), user1.hashCode());
//    }
//
//    @Test
//    public void testHashCodeDifferentUser() {
//        assertNotEquals(user1.hashCode(), user2.hashCode());
//    }
//
//    @Test
//    public void testDiffHashCode() {
//        assertNotEquals(user1.hashCode(), differentUser.hashCode());
//    }
//
//    @Test
//    public void testToString() {
//        assertEquals("User{id=0, notes=[], name='Greg'}", user1.toString());
//    }
//}
