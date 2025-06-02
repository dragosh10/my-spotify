// src/main/java/com/myspotify/model/User.java
package model; // ATENȚIE: Acest pachet trebuie să fie corect!

public class User {
    private int userId;
    private String username;
    private String passwordHash; // Vom stoca hash-ul parolei

    // Constructor complet (folosit la citirea din baza de date)
    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Constructor pentru crearea unui user nou, unde ID-ul este generat de baza de date
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Setters (pentru cazurile când ai nevoie să modifici atributele, în special ID-ul după inserție)
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                '}';
    }
}