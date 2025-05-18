package com.example.admintoolsandreporting.model;


import com.example.admintoolsandreporting.util.Constants;
import lombok.Data;

// This class represents an Admin with potentially elevated permissions.
// For this example, it's conceptual. Real auth would be needed.
@Data
public class Admin {
    private String username;
    // In a real app, store a hashed password, not plain text
    private String password; // Not used in this example's logic directly

    public Admin(String username) {
        this.username = username;
    }

    public boolean hasElevatedPermissions() {
        // Simple check for demonstration
        return Constants.ADMIN_USERNAME.equals(this.username);
    }
}
