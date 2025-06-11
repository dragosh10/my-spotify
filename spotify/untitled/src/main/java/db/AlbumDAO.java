// src/main/java/db/AlbumDAO.java
package db;

import model.Album;
import model.Song;
import java.time.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private Connection connection;

    public AlbumDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Album addAlbum(Album album) {
        String sql = "INSERT INTO Albums (album_name, artist_id, release_year, image_path) VALUES (?, ?, ?, ?) RETURNING album_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, album.getTitle());
            stmt.setInt(2, album.getArtistId());
            if (album.getReleaseYear() != null) {
                stmt.setInt(3, album.getReleaseYear());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, album.getImagePath());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    album.setAlbumId(rs.getInt(1));
                    return album;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Album getAlbumById(int albumId) {
        String sql = "SELECT * FROM Albums WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, albumId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Album(
                            rs.getInt("album_id"),
                            rs.getString("album_name"),
                            rs.getInt("artist_id"),
                            rs.getInt("release_year"),
                            rs.getString("image_path"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Album> getAlbumsByArtist(int artistId) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM Albums WHERE artist_id = ? ORDER BY release_year DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    albums.add(new Album(
                            rs.getInt("album_id"),
                            rs.getString("album_name"),
                            rs.getInt("artist_id"),
                            rs.getObject("release_year", Integer.class),
                            rs.getString("image_path")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public List<Album> getAllAlbums() {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM Albums";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Album album = new Album(
                        rs.getInt("album_id"),
                        rs.getString("album_name"),
                        rs.getInt("artist_id"),
                        rs.getObject("release_year", Integer.class),
                        rs.getString("image_path"));
                albums.add(album);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public boolean updateAlbum(Album album) {
        String sql = "UPDATE Albums SET album_name = ?, artist_id = ?, release_year = ? WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, album.getAlbumName());
            stmt.setInt(2, album.getArtistId());
            if (album.getReleaseYear() != null) {
                stmt.setInt(3, album.getReleaseYear());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, album.getAlbumId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAlbum(int albumId) {
        String sql = "DELETE FROM Albums WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, albumId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Song> getSongsInAlbum(int albumId) {
        List<Song> songs = new ArrayList<>();
        String query = "SELECT s.* FROM Songs s WHERE s.album_id = ?";
        System.out.println("Fetching songs for album ID: " + albumId);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, albumId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Duration duration;
                    try {
                        long seconds = rs.getLong("duration");
                        duration = Duration.ofSeconds(seconds);
                    } catch (Exception e) {
                        duration = Duration.ZERO;
                        System.err.println("Error parsing duration for song ID: " + rs.getInt("song_id"));
                    }

                    Song song = new Song(
                            rs.getInt("song_id"),
                            rs.getString("title"),
                            rs.getInt("artist_id"),
                            rs.getInt("album_id"),
                            duration,
                            rs.getString("file_path"));
                    songs.add(song);
                    System.out.println("Added song: " + song.getTitle() + " (ID: " + song.getSongId() + ")");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching songs for album: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Found " + songs.size() + " songs for album ID: " + albumId);
        return songs;
    }
}