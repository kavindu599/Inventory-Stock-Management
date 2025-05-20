package com.example.admintoolsandreporting.model;


import com.example.admintoolsandreporting.util.Constants;
import lombok.Data;
//Admin

@Data
public class Admin {
    private String username;
    private String password;

    public Admin(String username) {
        this.username = username;
    }

    public boolean hasElevatedPermissions() {
        return Constants.ADMIN_USERNAME.equals(this.username);
    }
}
