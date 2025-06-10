// src/main/java/model/Playlist.java
package model;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private int playlistId;
    private int userId;
    private String name;
    private List<Song> songs; // Această listă este esențială pentru a ține melodiile în memorie

    public Playlist(int playlistId, int userId, String name) {
        this.playlistId = playlistId;
        this.userId = userId;
        this.name = name;
        this.songs = new ArrayList<>(); // Inițializăm lista
    }

    public Playlist(int userId, String name) { // Constructor pentru playlist nou fără ID încă
        this.userId = userId;
        this.name = name;
        this.songs = new ArrayList<>();
    }

    // Getters and Setters
    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) { // Metodă utilitară pentru a adăuga o melodie în lista internă
        if (this.songs == null) { // Asigură-te că lista e inițializată
            this.songs = new ArrayList<>();
        }
        // Deși adăugăm în DB, e bine să o adăugăm și în lista locală pentru a o afișa imediat
        if (!this.songs.contains(song)) { // Evită duplicatele (necesită hashCode/equals în Song)
            this.songs.add(song);
        }
    }

    public void removeSong(Song song) { // Metodă utilitară pentru a elimina o melodie din lista internă
        if (this.songs != null) {
            this.songs.remove(song);
        }
    }

    @Override
    public String toString() {
        return name; // Foarte important pentru a afișa numele playlistului în ListView
    }
}