// src/main/java/org.example/Main.java
package org.example;

import model.Artist;
import model.Album;
import model.User;
import model.Song;
import model.Playlist;
import db.ArtistDAO;
import db.AlbumDAO;
import db.SongDAO;
import db.UserDAO;
import db.PlaylistDAO;
import db.PlaylistSongsDAO;
import db.DatabaseConnection;
import util.MusicPlayer;

import java.io.File;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Inițializarea DAO-urilor va apela DatabaseConnection.getConnection() prima dată
        ArtistDAO artistDao = new ArtistDAO();
        AlbumDAO albumDao = new AlbumDAO();
        SongDAO songDao = new SongDAO();
        UserDAO userDao = new UserDAO();
        PlaylistDAO playlistDao = new PlaylistDAO();
        PlaylistSongsDAO playlistSongsDao = new PlaylistSongsDAO();
        MusicPlayer musicPlayer = new MusicPlayer();

        System.out.println("--- Începerea testelor DAO și redare melodie ---");

        Artist addedArtist = null;
        Album addedAlbum = null;
        Song addedSong1 = null;
        Song addedSong2 = null;
        User addedUser = null;
        Playlist addedPlaylist = null;

        try {
            // --- Testare UserDAO ---
            System.out.println("\n--- Testare UserDAO ---");
            String username1 = "testuser_" + System.currentTimeMillis();
            User newUser1 = new User(username1, "parolaSecurizata");
            addedUser = userDao.addUser(newUser1);
            if (addedUser != null) {
                System.out.println("Utilizator adăugat cu succes: " + addedUser);
            } else {
                System.out.println("Eroare la adăugarea utilizatorului.");
            }

            System.out.println("\n--- Testare adăugare utilizator existent (username) ---");
            if (addedUser != null) {
                User duplicateUser = new User(addedUser.getUsername(), "altaparola");
                try {
                    userDao.addUser(duplicateUser);
                    System.out.println("--- EROARE: Utilizatorul existent a fost adăugat cu succes, constrângerea UNIQUE a eșuat! ---");
                } catch (SQLException e) {
                    System.out.println("✅ OK: Eroare SQL la adăugarea unui utilizator existent (AȘA TREBUIE SĂ FIE): " + e.getMessage());
                }
            }


            // --- Pre-condiții: Adăugăm un Artist și un Album valid ---
            System.out.println("\n--- Pre-condiții: Adăugare Artist și Album ---");
            Artist newArtist = new Artist("Artist pentru Melodii " + System.currentTimeMillis());
            addedArtist = artistDao.addArtist(newArtist);
            if (addedArtist == null) {
                System.out.println("Eroare: Nu s-a putut adăuga artistul necesar pentru teste Song.");
            } else {
                System.out.println("Artist pre-condiție adăugat: " + addedArtist);
            }

            Album newAlbum = new Album("Album pentru Melodii " + System.currentTimeMillis(), addedArtist != null ? addedArtist.getArtistId() : 1, 2023);
            addedAlbum = albumDao.addAlbum(newAlbum);
            if (addedAlbum == null) {
                System.out.println("Eroare: Nu s-a putut adăuga albumul necesar pentru teste Song.");
            } else {
                System.out.println("Album pre-condiție adăugat: " + addedAlbum);
            }


            // --- Testare SongDAO ---
            System.out.println("\n--- Testare SongDAO ---");
            String testMp3Path = System.getProperty("user.dir") + File.separator + "test_song.mp3";
            File actualMp3File = new File(testMp3Path);
            if (!actualMp3File.exists()) {
                System.err.println("ATENȚIE: Fișierul MP3 pentru test ('test_song.mp3') NU există la calea: " + testMp3Path);
                System.err.println("Te rog, pune un fișier 'test_song.mp3' în rădăcina proiectului tău pentru a testa redarea!");
                testMp3Path = "/path/to/non_existent_mp3_for_test_" + System.currentTimeMillis() + ".mp3";
            } else {
                System.out.println("Fișier MP3 pentru test găsit: " + testMp3Path);
            }

            System.out.println("\n--- Testare adăugare melodie (cu album) ---");
            if (addedArtist != null && addedAlbum != null) {
                Song newSong1 = new Song(
                        "Melodie Cu Album " + System.currentTimeMillis(),
                        addedArtist.getArtistId(),
                        addedAlbum.getAlbumId(),
                        Duration.ofMinutes(0).plusSeconds(10),
                        testMp3Path
                );
                addedSong1 = songDao.addSong(newSong1);
                if (addedSong1 != null) {
                    System.out.println("Melodie adăugată cu succes: " + addedSong1);
                } else {
                    System.out.println("Nu s-a putut adăuga melodia cu album.");
                }
            } else {
                System.out.println("Sărind testul de adăugare melodie cu album din cauza pre-condițiilor lipsă.");
            }

            System.out.println("\n--- Testare adăugare melodie (single) ---");
            if (addedArtist != null) {
                Song newSong2 = new Song(
                        "Melodie Single " + System.currentTimeMillis(),
                        addedArtist.getArtistId(),
                        null,
                        Duration.ofSeconds(210),
                        "/path/to/music/song_single_" + System.currentTimeMillis() + ".mp3"
                );
                addedSong2 = songDao.addSong(newSong2);
                if (addedSong2 != null) {
                    System.out.println("Melodie single adăugată cu succes: " + addedSong2);
                } else {
                    System.out.println("Nu s-a putut adăuga melodia single.");
                }
            } else {
                System.out.println("Sărind testul de adăugare melodie single din cauza pre-condițiilor lipsă.");
            }

            System.out.println("\n--- Testare adăugare melodie existentă (titlu + artist) ---");
            if (addedSong1 != null) {
                String duplicateFilePath = "/path/to/duplicate_song_" + System.currentTimeMillis() + ".mp3";
                Song duplicateSong = new Song(
                        addedSong1.getTitle(),
                        addedSong1.getArtistId(),
                        addedSong1.getAlbumId(),
                        Duration.ofSeconds(100),
                        duplicateFilePath
                );
                try {
                    songDao.addSong(duplicateSong);
                    System.out.println("--- EROARE: Melodie existentă adăugată cu succes, constrângerea UNIQUE a eșuat! ---");
                } catch (SQLException e) {
                    System.out.println("✅ OK: Eroare SQL la adăugarea unei melodii existente (AȘA TREBUIE SĂ FIE): " + e.getMessage());
                }
            }

            System.out.println("\n--- Testare căutare melodie după ID ---");
            if (addedSong1 != null) {
                Song foundSongById = songDao.getSongById(addedSong1.getSongId());
                if (foundSongById != null) {
                    System.out.println("Melodie găsită după ID " + addedSong1.getSongId() + ": " + foundSongById);
                } else {
                    System.out.println("Melodia cu ID " + addedSong1.getSongId() + " nu a fost găsită.");
                }
            }

            System.out.println("\n--- Testare căutare melodii după Artist ID ---");
            if (addedArtist != null) {
                List<Song> songsByArtist = songDao.getSongsByArtist(addedArtist.getArtistId());
                if (!songsByArtist.isEmpty()) {
                    System.out.println("Melodii pentru artistul cu ID " + addedArtist.getArtistId() + " (" + songsByArtist.size() + "):");
                    songsByArtist.forEach(song -> System.out.println(" - " + song.getTitle() + " (ID: " + song.getSongId() + ")"));
                } else {
                    System.out.println("Nicio melodie găsită pentru artistul cu ID " + addedArtist.getArtistId() + ".");
                }
            }

            System.out.println("\n--- Testare căutare melodii după Album ID ---");
            if (addedAlbum != null) {
                List<Song> songsByAlbum = songDao.getSongsByAlbum(addedAlbum.getAlbumId());
                if (!songsByAlbum.isEmpty()) {
                    System.out.println("Melodii pentru albumul cu ID " + addedAlbum.getAlbumId() + " (" + songsByAlbum.size() + "):");
                    songsByAlbum.forEach(song -> System.out.println(" - " + song.getTitle() + " (ID: " + song.getSongId() + ")"));
                } else {
                    System.out.println("Nicio melodie găsită pentru albumul cu ID " + addedAlbum.getAlbumId() + ".");
                }
            }

            System.out.println("\n--- Testare actualizare melodie ---");
            if (addedSong1 != null) {
                String oldTitle = addedSong1.getTitle();
                addedSong1.setTitle("Melodie Actualizată " + System.currentTimeMillis());
                addedSong1.setDuration(Duration.ofMinutes(5).plusSeconds(10));
                boolean updated = songDao.updateSong(addedSong1);
                if (updated) {
                    System.out.println("Melodie actualizată cu succes de la '" + oldTitle + "' la '" + addedSong1.getTitle() + "'");
                    Song recheckedSong = songDao.getSongById(addedSong1.getSongId());
                    System.out.println("Verificare: Melodia actualizată este: " + recheckedSong);
                } else {
                    System.out.println("Eroare la actualizarea melodiei.");
                }
            }


            // --- Testare PlaylistDAO și PlaylistSongsDAO ---
            System.out.println("\n--- Testare Playlists și PlaylistSongs DAO ---");

            if (addedUser == null) {
                String tempUsername = "temp_user_" + System.currentTimeMillis();
                addedUser = userDao.addUser(new User(tempUsername, "temp_pass"));
                System.out.println("Utilizator temporar adăugat pentru testarea playlist-urilor: " + addedUser);
            }
            if (addedUser == null) {
                System.err.println("Eroare: Nu s-a putut crea un utilizator pentru testele playlist-urilor.");
                return;
            }

            System.out.println("\n--- Adăugare Playlist ---");
            Playlist newPlaylist = new Playlist("Playlistul Meu " + System.currentTimeMillis(), addedUser.getUserId());
            addedPlaylist = playlistDao.addPlaylist(newPlaylist);
            if (addedPlaylist != null) {
                System.out.println("Playlist adăugat: " + addedPlaylist);
            } else {
                System.out.println("Eroare la adăugarea playlist-ului.");
            }

            System.out.println("\n--- Adăugare Melodie în Playlist ---");
            if (addedPlaylist != null && addedSong1 != null) {
                boolean addedToPlaylist = playlistSongsDao.addSongToPlaylist(addedPlaylist.getPlaylistId(), addedSong1.getSongId(), 1);
                if (addedToPlaylist) {
                    System.out.println("Melodia '" + addedSong1.getTitle() + "' adăugată în playlist-ul '" + addedPlaylist.getName() + "'");
                } else {
                    System.out.println("Eroare la adăugarea melodiei în playlist sau era deja prezentă.");
                }
            } else {
                System.out.println("Sărind testul de adăugare melodie în playlist din cauza datelor lipsă.");
            }

            System.out.println("\n--- Obținere Melodii din Playlist ---");
            if (addedPlaylist != null) {
                List<Song> songsInPlaylist = playlistSongsDao.getSongsInPlaylist(addedPlaylist.getPlaylistId());
                if (!songsInPlaylist.isEmpty()) {
                    System.out.println("Melodii în playlist-ul '" + addedPlaylist.getName() + "':");
                    songsInPlaylist.forEach(song -> System.out.println(" - " + song.getTitle() + " (ID: " + song.getSongId() + ")"));
                } else {
                    System.out.println("Nicio melodie găsită în playlist-ul '" + addedPlaylist.getName() + "'.");
                }
            }

            System.out.println("\n--- Actualizare Nume Playlist ---");
            if (addedPlaylist != null) {
                String oldPlaylistName = addedPlaylist.getName();
                addedPlaylist.setName("Playlistul Meu Actualizat " + System.currentTimeMillis());
                boolean updatedPlaylist = playlistDao.updatePlaylist(addedPlaylist);
                if (updatedPlaylist) {
                    System.out.println("Nume playlist actualizat de la '" + oldPlaylistName + "' la '" + addedPlaylist.getName() + "'");
                } else {
                    System.out.println("Eroare la actualizarea playlist-ului.");
                }
            }

            System.out.println("\n--- Ștergere Melodie din Playlist ---");
            if (addedPlaylist != null && addedSong1 != null) {
                boolean removedFromPlaylist = playlistSongsDao.removeSongFromPlaylist(addedPlaylist.getPlaylistId(), addedSong1.getSongId());
                if (removedFromPlaylist) {
                    System.out.println("Melodia '" + addedSong1.getTitle() + "' ștearsă din playlist-ul '" + addedPlaylist.getName() + "'");
                } else {
                    System.out.println("Eroare la ștergerea melodiei din playlist sau nu era prezentă.");
                }
            }

            System.out.println("\n--- Verificare Melodii din Playlist după ștergere ---");
            if (addedPlaylist != null) {
                List<Song> songsAfterRemoval = playlistSongsDao.getSongsInPlaylist(addedPlaylist.getPlaylistId());
                if (songsAfterRemoval.isEmpty()) {
                    System.out.println("Playlist-ul '" + addedPlaylist.getName() + "' este acum gol. ✅");
                } else {
                    System.out.println("Eroare: Melodii rămase în playlist după ștergere. ❌");
                    songsAfterRemoval.forEach(song -> System.out.println(" - " + song.getTitle()));
                }
            }


            // --- Testare redare efectivă a melodiei adăugate ---
            System.out.println("\n--- Testare redare melodie adăugată ---");
            if (addedSong1 != null) {
                File fileToPlay = new File(addedSong1.getFilePath());
                if (fileToPlay.exists() && fileToPlay.isFile()) {
                    System.out.println("Începe redarea melodiei adăugate: " + addedSong1.getTitle() + " de la calea: " + addedSong1.getFilePath());
                    musicPlayer.play(addedSong1.getFilePath());

                    long waitTimeMillis = addedSong1.getDuration().toMillis() + 1000;
                    if (waitTimeMillis > 15000) waitTimeMillis = 15000;
                    System.out.println("Aștept " + waitTimeMillis + " ms pentru redare...");
                    Thread.sleep(waitTimeMillis);
                    musicPlayer.stop();
                    System.out.println("Redare melodie adăugată terminată.");
                } else {
                    System.out.println("ATENȚIE: Fișierul pentru melodia '" + addedSong1.getTitle() + "' nu există la calea: " + addedSong1.getFilePath() + ". Nu se poate reda.");
                }
            } else {
                System.out.println("Nicio melodie nu a putut fi adăugată sau găsită pentru a testa redarea.");
            }


            // --- Teste de curățare a bazei de date ---
            System.out.println("\n--- Teste de curățare a bazei de date ---");

            if (addedPlaylist != null) {
                boolean deletedPlaylist = playlistDao.deletePlaylist(addedPlaylist.getPlaylistId());
                System.out.println("Playlist șters: " + deletedPlaylist);
            }

            if (addedSong1 != null) {
                boolean deletedSong1 = songDao.deleteSong(addedSong1.getSongId());
                System.out.println("Melodie 1 ștersă: " + deletedSong1);
            }
            if (addedSong2 != null) {
                boolean deletedSong2 = songDao.deleteSong(addedSong2.getSongId());
                System.out.println("Melodie 2 ștersă: " + deletedSong2);
            }

            if (addedAlbum != null) {
                boolean deletedAlbum = albumDao.deleteAlbum(addedAlbum.getAlbumId());
                System.out.println("Album șters: " + deletedAlbum);
            }

            if (addedArtist != null) {
                boolean deletedArtist = artistDao.deleteArtist(addedArtist.getArtistId());
                System.out.println("Artist șters: " + deletedArtist);
            }


        } catch (SQLException e) {
            System.err.println("A apărut o eroare SQL în testele: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread-ul principal a fost întrerupt: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("\n--- Toate testele încheiate ---");
            musicPlayer.stop();
            DatabaseConnection.closeConnection(); // Închide conexiunea la final
        }
    }
}