// src/main/java/db/SongDAO.java
package db;

import model.Song;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SongDAO {
    private Connection connection;

    public SongDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Song addSong(Song song) {
        String sql = "INSERT INTO songs (title, artist_id, album_id, duration, file_path) VALUES (?, ?, ?, ?, ?) RETURNING song_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, song.getTitle());
            stmt.setInt(2, song.getArtistId());
            if (song.getAlbumId() != null) {
                stmt.setInt(3, song.getAlbumId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, (int) song.getDuration().toSeconds());
            stmt.setString(5, song.getFilePath());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    song.setSongId(rs.getInt(1));
                    return song;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Song getSongById(int songId) {
        String sql = "SELECT * FROM songs WHERE song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractSongFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Song> getAllSongs() {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                songs.add(extractSongFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<Song> searchSongs(String query) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE title ILIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(extractSongFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public boolean updateSong(Song song) {
        String sql = "UPDATE songs SET title = ?, artist_id = ?, album_id = ?, duration = ?, file_path = ? WHERE song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, song.getTitle());
            stmt.setInt(2, song.getArtistId());
            if (song.getAlbumId() != null) {
                stmt.setInt(3, song.getAlbumId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, (int) song.getDuration().toSeconds());
            stmt.setString(5, song.getFilePath());
            stmt.setInt(6, song.getSongId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSong(int songId) {
        String sql = "DELETE FROM songs WHERE song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, songId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Song> getSongsByArtist(int artistId) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT * FROM songs WHERE artist_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, artistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    songs.add(extractSongFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    public List<Song> getSongsByAlbum(int albumId) {
        List<Song> songs = new ArrayList<>();
        String query = "SELECT * FROM Songs WHERE album_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, albumId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Duration duration = Duration.ofSeconds(rs.getLong("duration"));
                Song song = new Song(
                    rs.getInt("song_id"),
                    rs.getString("title"),
                    rs.getInt("artist_id"),
                    rs.getInt("album_id"),
                    duration,
                    rs.getString("file_path")
                );
                songs.add(song);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songs;
    }

    private Song extractSongFromResultSet(ResultSet rs) throws SQLException {
        // Retrieve the duration as a String and parse it
        String durationString = rs.getString("duration");
        Duration duration = parseDurationString(durationString);

        return new Song(
                rs.getInt("song_id"),
                rs.getString("title"),
                rs.getInt("artist_id"),
                rs.getInt("album_id"),
                duration,
                rs.getString("file_path")
        );
    }

    // Helper method to parse "HH:MM:SS" or "MM:SS" duration string to java.time.Duration
    private Duration parseDurationString(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ZERO;
        }
        String[] parts = durationStr.split(":");
        if (parts.length == 3) { // HH:MM:SS
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            long seconds = Long.parseLong(parts[2]);
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        } else if (parts.length == 2) { // MM:SS
            long minutes = Long.parseLong(parts[0]);
            long seconds = Long.parseLong(parts[1]);
            return Duration.ofMinutes(minutes).plusSeconds(seconds);
        } else { // Assume it's just seconds or handle other formats as needed
            try {
                return Duration.ofSeconds(Long.parseLong(durationStr));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse duration string: " + durationStr);
                return Duration.ZERO;
            }
        }
    }
}