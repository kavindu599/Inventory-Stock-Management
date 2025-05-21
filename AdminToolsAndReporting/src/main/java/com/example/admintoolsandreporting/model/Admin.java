package com.example.admintoolsandreporting.model;


import com.example.admintoolsandreporting.util.Constants;
import lombok.Data;


@Data
public class Admin {
    private String username;

    private String password;

    public Admin(String username) {
        this.username = username;
    }

    public boolean hasElevatedPermissions() {
        // Simple check for demonstration
        return Constants.ADMIN_USERNAME.equals(this.username);
    }
}
