// src/main/java/model/Song.java
package model;

import java.time.Duration;

public class Song {
    private int songId;
    private String title;
    private int artistId;
    private Integer albumId;
    private Duration duration;
    private String filePath;

    public Song(int songId, String title, int artistId, Integer albumId, Duration duration, String filePath) {
        this.songId = songId;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.duration = duration;
        this.filePath = filePath;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songId=" + songId +
                ", title='" + title + '\'' +
                ", artistId=" + artistId +
                ", albumId=" + albumId +
                ", duration=" + duration +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}