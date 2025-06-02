// src/main/java/model/Album.java
package model;

public class Album {
    private int albumId;
    private String title;
    private int artistId; // Referință către Artist
    private Integer releaseYear; // Integer pentru a permite NULL

    // Constructor complet, util la citirea din baza de date
    public Album(int albumId, String title, int artistId, Integer releaseYear) {
        this.albumId = albumId;
        this.title = title;
        this.artistId = artistId;
        this.releaseYear = releaseYear;
    }

    // Constructor pentru crearea unui Album nou, unde ID-ul este generat de DB
    public Album(String title, int artistId, Integer releaseYear) {
        this.title = title;
        this.artistId = artistId;
        this.releaseYear = releaseYear;
    }

    // Getters
    public int getAlbumId() {
        return albumId;
    }

    public String getTitle() {
        return title;
    }

    public int getArtistId() {
        return artistId;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    // Setters (pentru cazurile când ID-ul este generat de DB la inserare sau pentru actualizări)
    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public void setTitle(String title) {
        this.title = title;
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
                ", title='" + title + '\'' +
                ", artistId=" + artistId +
                ", releaseYear=" + releaseYear +
                '}';
    }
}