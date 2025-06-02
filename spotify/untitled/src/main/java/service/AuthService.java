package service;

import db.UserDAO;
import model.User;
import java.sql.SQLException;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public User register(String username, String password) throws SQLException {
        // Verifică dacă utilizatorul există deja
        if (userDAO.getUserByUsername(username) != null) {
            return null;
        }

        // Creează un nou utilizator
        return userDAO.createUser(username, password);
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
} 