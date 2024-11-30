package server;

import commons.User;
import org.springframework.web.bind.annotation.*;
import server.database.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/")
    public String createUser(@RequestParam String name) {
        User user = new User(name);
        userRepository.save(user);
        return "User created successfully!";
    }

    @GetMapping("/{userid}")
    public User getUser(@PathVariable String userid) {
        User user = userRepository.findById(Long.parseLong(userid));
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return user;
    }
}
