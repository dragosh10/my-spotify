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

        // Creează un nou utilizator cu hash-ul parolei
        User newUser = new User(username, password);  // În practică, aici ar trebui să facem hash la parolă
        return userDAO.addUser(newUser);
    }

    public User login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsername(username);
        if (user != null && user.getPasswordHash().equals(password)) {  // În practică, aici ar trebui să comparăm hash-urile
            return user;
        }
        return null;
    }
}

