package com.example.loginapp.controller;

import com.example.loginapp.model.User;
import com.example.loginapp.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:5500", "http://localhost:63342"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/users")
public class UserController {


    @Autowired
    private UserService userService;

    // ✅ Get user profile by username
    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) throws IOException {
        Optional<User> user = userService.findUserByUsername(username);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Return raw content of users.txt (for login.html to read)
    @GetMapping("/all")
    public ResponseEntity<String> getAllUsersFileContent() {
        try {
            // Use absolute path or keep relative if running from project root
            Path path = Paths.get("users.txt"); // or full path: "C:/Users/E-ZONE/Downloads/springOne/loginapp/users.txt"
            String content = Files.readString(path);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading users.txt: " + e.getMessage());
        }
    }


    // ✅ Create or update user profile
    @PostMapping
    public ResponseEntity<String> createOrUpdateUser(@RequestBody User user) throws IOException {
        userService.saveUser(user);
        return ResponseEntity.ok("User saved successfully");
    }


    @PutMapping("/{username}")
    public ResponseEntity<String> updateUser(@PathVariable String username,
                                             @RequestBody User updatedUser,
                                             HttpSession session) throws IOException {
        if (!username.equals(updatedUser.getUsername())) {
            return ResponseEntity.badRequest().body("Username mismatch");
        }

        // ✅ Save the updated user details to file
        userService.saveUser(updatedUser);

        // ✅ Update session if the currently logged-in user is the one being updated
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser != null && sessionUser.getUsername().equals(username)) {
            session.setAttribute("loggedInUser", updatedUser);
        }

        return ResponseEntity.ok("User updated successfully");
    }


    // ✅ Delete user by username
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) throws IOException {
        boolean deleted = userService.deleteUser(username);
        return deleted
                ? ResponseEntity.ok("User deleted successfully")
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User loginRequest, HttpSession session) throws IOException {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        Optional<User> userOpt = userService.validateUserLogin(username, password);
        if (userOpt.isPresent()) {
            session.setAttribute("loggedInUser", userOpt.get());
            return ResponseEntity.ok(userOpt.get()); // Send back user info
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }


    // ✅ Get session user profile
    @GetMapping("/session-profile")
    public ResponseEntity<User> getSessionProfile(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        return (user != null)
                ? ResponseEntity.ok(user)
                : ResponseEntity.status(401).build();
    }

    // ✅ Logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }
}
