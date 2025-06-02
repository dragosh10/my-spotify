// src/main/java/db/AlbumDAO.java
package db;

import model.Album;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlbumDAO {
    private Connection connection;

    public AlbumDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Album addAlbum(Album album) {
        String sql = "INSERT INTO albums (title, artist_id, release_year) VALUES (?, ?, ?) RETURNING album_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, album.getTitle());
            stmt.setInt(2, album.getArtistId());
            if (album.getReleaseYear() != null) {
                stmt.setInt(3, album.getReleaseYear());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

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
        String sql = "SELECT * FROM albums WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, albumId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Album(
                            rs.getInt("album_id"),
                            rs.getString("title"),
                            rs.getInt("artist_id"),
                            rs.getInt("release_year")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Album> getAlbumsByArtist(int artistId) {
        List<Album> albums = new ArrayList<>();
        String sql = "SELECT * FROM albums WHERE artist_id = ? ORDER BY release_year DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    albums.add(new Album(
                            rs.getInt("album_id"),
                            rs.getString("title"),
                            rs.getInt("artist_id"),
                            rs.getInt("release_year")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albums;
    }

    public boolean updateAlbum(Album album) {
        String sql = "UPDATE albums SET title = ?, artist_id = ?, release_year = ? WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, album.getTitle());
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
        String sql = "DELETE FROM albums WHERE album_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, albumId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}