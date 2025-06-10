// src/main/java/db/PlaylistSongsDAO.java
package db;

import model.Song;
import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class PlaylistSongsDAO {
    private Connection connection;

    public PlaylistSongsDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean addSongToPlaylist(int playlistId, int songId, int position) {
        String sql = "INSERT INTO PlaylistSongs (playlist_id, song_id, order_in_playlist) VALUES (?, ?, ?)";
        System.out.println("Adding song " + songId + " to playlist " + playlistId + " at position " + position);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            stmt.setInt(3, position);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding song to playlist: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSongFromPlaylist(int playlistId, int songId) {
        String sql = "DELETE FROM PlaylistSongs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Song> getSongsInPlaylist(int playlistId) {
        List<Song> songs = new ArrayList<>();
        String sql = "SELECT s.* FROM Songs s " +
                "JOIN PlaylistSongs ps ON s.song_id = ps.song_id " +
                "WHERE ps.playlist_id = ? " +
                "ORDER BY ps.order_in_playlist ASC";
        System.out.println("Fetching songs for playlist " + playlistId);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Get the interval string from PostgreSQL
                    String intervalStr = rs.getString("duration");
                    // Convert HH:MM:SS or MM:SS duration string to Duration
                    Duration duration = parseDuration(intervalStr);

                    Song song = new Song(
                            rs.getInt("song_id"),
                            rs.getString("title"),
                            rs.getInt("artist_id"),
                            rs.getInt("album_id"),
                            duration,
                            rs.getString("file_path")
                    );
                    songs.add(song);
                    System.out.println("Found song: " + song.getTitle() + " (ID: " + song.getSongId() + ")");
                }
            }
            System.out.println("Total songs found in playlist: " + songs.size());
        } catch (SQLException e) {
            System.err.println("Error getting songs from playlist: " + e.getMessage());
            e.printStackTrace();
        }
        return songs;
    }

    private Duration parseDuration(String intervalStr) {
        if (intervalStr == null || intervalStr.isEmpty()) {
            return Duration.ZERO;
        }
        try {
            // PostgreSQL INTERVAL format is like "3 minutes 14 seconds" or "4 minutes"
            // Split into parts
            String[] parts = intervalStr.split(" ");
            long totalSeconds = 0;

            for (int i = 0; i < parts.length; i++) {
                String value = parts[i];
                if (i + 1 < parts.length) {
                    String unit = parts[i + 1].toLowerCase();
                    try {
                        long numVal = Long.parseLong(value);
                        switch (unit) {
                            case "hours":
                            case "hour":
                                totalSeconds += numVal * 3600;
                                break;
                            case "minutes":
                            case "minute":
                                totalSeconds += numVal * 60;
                                break;
                            case "seconds":
                            case "second":
                                totalSeconds += numVal;
                                break;
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    i++; // Skip the unit in next iteration
                }
            }
            return Duration.ofSeconds(totalSeconds);
        } catch (Exception e) {
            System.err.println("Error parsing duration: " + intervalStr);
            e.printStackTrace();
            return Duration.ZERO;
        }
    }

    public boolean updateSongPosition(int playlistId, int songId, int newPosition) {
        String sql = "UPDATE PlaylistSongs SET order_in_playlist = ? WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newPosition);
            stmt.setInt(2, playlistId);
            stmt.setInt(3, songId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSongInPlaylist(int playlistId, int songId) {
        String sql = "SELECT COUNT(*) FROM PlaylistSongs WHERE playlist_id = ? AND song_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            stmt.setInt(2, songId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getPlaylistSize(int playlistId) {
        String sql = "SELECT COUNT(*) FROM PlaylistSongs WHERE playlist_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, playlistId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}