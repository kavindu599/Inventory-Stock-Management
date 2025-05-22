package com.example.loginapp.services;

import com.example.loginapp.model.User;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class UserService {

    private static final String FILE_PATH = "users.txt";

    // Convert CSV line to User
    private User parseUser(String line) {
        String[] parts = line.split(",");
        if (parts.length < 6) return null;
        return new User(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    // Convert User to CSV line
    private String userToLine(User user) {
        return String.join(",",
                user.getUsername(),
                user.getPassword(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getPhone());
    }

    // ✅ Get all users
    public List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            System.out.println("File not found: " + FILE_PATH);
            return users;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                User user = parseUser(line);
                if (user != null) users.add(user);
            }
        }

        return users;
    }

    // ✅ Find user by username
    public Optional<User> findUserByUsername(String username) throws IOException {
        return getAllUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    // ✅ Validate login by username or email
    public Optional<User> validateUserLogin(String usernameOrEmail, String password) throws IOException {
        return getAllUsers().stream()
                .filter(u -> (u.getUsername().equalsIgnoreCase(usernameOrEmail)
                        || u.getEmail().equalsIgnoreCase(usernameOrEmail))
                        && u.getPassword().equals(password))
                .findFirst();
    }

    // ✅ Save or update user
    public void saveUser(User updatedUser) throws IOException {
        List<User> users = getAllUsers();
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) users.add(updatedUser);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                bw.write(userToLine(user));
                bw.newLine();
            }
        }
    }

    // ✅ Delete user by username
    public boolean deleteUser(String username) throws IOException {
        List<User> users = getAllUsers();
        boolean removed = users.removeIf(u -> u.getUsername().equals(username));

        if (removed) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (User user : users) {
                    bw.write(userToLine(user));
                    bw.newLine();
                }
            }
        }

        return removed;
    }
}
