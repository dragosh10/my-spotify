import db.DatabaseConnection;
import db.UserDAO;
import model.User;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabase {
    public static void main(String[] args) {
        try {
            // Test database connection
            System.out.println("Testing database connection...");
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Database connection successful!");
            }

            // Test user operations
            System.out.println("\nTesting user operations...");
            UserDAO userDAO = new UserDAO();
            
            // Create a test user
            User testUser = new User("testuser", "testpass");
            User addedUser = userDAO.addUser(testUser);
            if (addedUser != null) {
                System.out.println("Successfully added test user with ID: " + addedUser.getUserId());
            }
            
            // Try to retrieve the user
            User retrievedUser = userDAO.getUserByUsername("testuser");
            if (retrievedUser != null) {
                System.out.println("Successfully retrieved user: " + retrievedUser.getUsername());
            }

            // Close the connection
            DatabaseConnection.closeConnection();
            System.out.println("\nDatabase connection closed.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 