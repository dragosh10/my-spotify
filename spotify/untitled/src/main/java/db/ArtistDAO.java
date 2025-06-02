// src/main/java/db/ArtistDAO.java
package db;

import model.Artist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtistDAO {
    private Connection connection;

    public ArtistDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Artist addArtist(Artist artist) {
        String sql = "INSERT INTO artists (name) VALUES (?) RETURNING artist_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artist.getName());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    artist.setArtistId(rs.getInt(1));
                    return artist;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Artist getArtistById(int artistId) {
        String sql = "SELECT * FROM artists WHERE artist_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Artist(
                            rs.getInt("artist_id"),
                            rs.getString("name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Artist> getAllArtists() {
        List<Artist> artists = new ArrayList<>();
        String sql = "SELECT * FROM artists ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                artists.add(new Artist(
                        rs.getInt("artist_id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return artists;
    }

    public boolean updateArtist(Artist artist) {
        String sql = "UPDATE artists SET name = ? WHERE artist_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, artist.getName());
            stmt.setInt(2, artist.getArtistId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteArtist(int artistId) {
        String sql = "DELETE FROM artists WHERE artist_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artistId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}