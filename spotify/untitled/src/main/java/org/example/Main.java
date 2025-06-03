package org.example;

import model.*;
import db.*;

import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            // Initialize DAOs
            UserDAO userDao = new UserDAO();
            ArtistDAO artistDao = new ArtistDAO();
            AlbumDAO albumDao = new AlbumDAO();
            SongDAO songDao = new SongDAO();
            PlaylistDAO playlistDao = new PlaylistDAO();
            PlaylistSongsDAO playlistSongsDao = new PlaylistSongsDAO();

            // Test User operations
            User user = new User("testUser" + System.currentTimeMillis(), "password123");
            user = userDao.addUser(user);
            System.out.println("Added user: " + user);

            // Test Artist operations
            Artist artist = new Artist("Test Artist " + System.currentTimeMillis());
            artist = artistDao.addArtist(artist);
            System.out.println("Added artist: " + artist);

            // Test Album operations
            Album album = new Album("Test Album " + System.currentTimeMillis(), artist.getArtistId(), 2025);
            album = albumDao.addAlbum(album);
            System.out.println("Added album: " + album);

            // Test Song operations
            Song song = new Song(0, "Test Song " + System.currentTimeMillis(), artist.getArtistId(), album.getAlbumId(), Duration.ofMinutes(3).plusSeconds(30), "test_song.mp3");
            song = songDao.addSong(song);
            System.out.println("Added song: " + song);

            // Test Playlist operations
            String playlistName = "My Test Playlist " + System.currentTimeMillis();
            Playlist playlist = playlistDao.createPlaylist(playlistName, user.getUserId());
            System.out.println("Created playlist: " + playlist);

            // Test PlaylistSongs operations
            playlistSongsDao.addSongToPlaylist(playlist.getPlaylistId(), song.getSongId(), 1);
            System.out.println("Added song to playlist");

            // Test retrieving data
            System.out.println("\nTesting retrieval operations:");
            System.out.println("Found user by username: " + userDao.getUserByUsername(user.getUsername()));
            System.out.println("Found artist: " + artistDao.getArtistById(artist.getArtistId()));
            System.out.println("Found album: " + albumDao.getAlbumById(album.getAlbumId()));
            System.out.println("Found song: " + songDao.getSongById(song.getSongId()));
            System.out.println("Found playlist: " + playlistDao.getPlaylistById(playlist.getPlaylistId()));
            System.out.println("Songs in playlist: " + playlistSongsDao.getSongsInPlaylist(playlist.getPlaylistId()));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while testing the DAOs", e);
        }
    }
}

