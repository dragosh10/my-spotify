package org.example;

import db.DatabaseConnection;
import db.UserDAO;
import model.User;
import java.sql.Connection;

public class TestDatabase {
    public static void main(String[] args) {
        try {
            // Test the connection
            Connection connection = DatabaseConnection.getConnection();
            if (connection != null) {
                System.out.println("Successfully connected to the database!");
            }

            // Initialize UserDAO
            UserDAO userDAO = new UserDAO();

            // Create a test user
            User testUser = new User("testuser", "password123");
            User createdUser = userDAO.addUser(testUser);
            if (createdUser != null) {
                System.out.println("Successfully created user: " + createdUser.getUsername());
            }

            // Try to retrieve the user
            User retrievedUser = userDAO.getUserByUsername("testuser");
            if (retrievedUser != null) {
                System.out.println("Successfully retrieved user: " + retrievedUser.getUsername());
            }

            // Close the connection
            DatabaseConnection.closeConnection();
            System.out.println("\nDatabase connection closed.");

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

