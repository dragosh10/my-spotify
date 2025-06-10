// src/main/java/model/Album.java
package model;

public class Album {
    private int albumId;
    private String albumName; // Changed from title to match database
    private int artistId;
    private Integer releaseYear;

    // Constructor complet, util la citirea din baza de date
    public Album(int albumId, String albumName, int artistId, Integer releaseYear) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.releaseYear = releaseYear;
    }

    // Constructor pentru crearea unui Album nou, unde ID-ul este generat de DB
    public Album(String albumName, int artistId, Integer releaseYear) {
        this.albumName = albumName;
        this.artistId = artistId;
        this.releaseYear = releaseYear;
    }

    // Getters
    public int getAlbumId() {
        return albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getTitle() {  // Added for compatibility with existing code
        return albumName;
    }

    public int getArtistId() {
        return artistId;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    // Setters
    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setTitle(String title) {  // Added for compatibility with existing code
        this.albumName = title;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    @Override
    public String toString() {
        return "Album{" +
                "albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", releaseYear=" + releaseYear +
                '}';
    }
}