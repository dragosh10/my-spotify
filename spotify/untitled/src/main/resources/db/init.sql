-- Acest script creează schema de bază de date pentru aplicația MySpotify.
-- Rulați acest script în baza de date 'myspotify' creată anterior.

-- DROP TABLE-uri EXISTENTE (Opțional, dar util pentru re-rulări în timpul dezvoltării)
-- Folosiți cu prudență în producție!
DROP TABLE IF EXISTS PlaylistSongs CASCADE;
DROP TABLE IF EXISTS Playlists CASCADE;
DROP TABLE IF EXISTS Songs CASCADE;
DROP TABLE IF EXISTS Albums CASCADE;
DROP TABLE IF EXISTS Artists CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- CREATE TABLES

-- Tabelul pentru utilizatori
CREATE TABLE Users (
                       user_id SERIAL PRIMARY KEY, -- ID-ul unic al utilizatorului (auto-increment)
                       username VARCHAR(50) UNIQUE NOT NULL, -- Numele de utilizator, trebuie să fie unic și să nu fie gol
                       password_hash VARCHAR(255) NOT NULL -- Hash-ul parolei, NU parola în clar!
);

-- Tabelul pentru artiști
CREATE TABLE Artists (
                         artist_id SERIAL PRIMARY KEY, -- ID-ul unic al artistului (auto-increment)
                         artist_name VARCHAR(255) UNIQUE NOT NULL -- Numele artistului, trebuie să fie unic și să nu fie gol
);

-- Tabelul pentru albume
-- Tabelul pentru albume
CREATE TABLE Albums (
                        album_id SERIAL PRIMARY KEY,
                        album_name VARCHAR(255) NOT NULL,
                        artist_id INT NOT NULL,
                        release_year INT,
                        FOREIGN KEY (artist_id) REFERENCES Artists(artist_id) ON DELETE CASCADE,
                        UNIQUE (album_name, artist_id) -- ADAUGĂ ACEASTĂ LINIE
);

-- Tabelul pentru melodii
CREATE TABLE Songs (
                       song_id SERIAL PRIMARY KEY, -- ID-ul unic al melodiei (auto-increment)
                       title VARCHAR(255) NOT NULL, -- Titlul melodiei
                       artist_id INT NOT NULL, -- ID-ul artistului principal al melodiei
                       album_id INT, -- ID-ul albumului din care face parte melodia (poate fi NULL dacă nu e parte dintr-un album)
                       duration INTERVAL, -- Durata melodiei (ex: '00:03:30' pentru 3 minute și 30 secunde)
                       file_path VARCHAR(512) UNIQUE NOT NULL, -- Calea absolută către fișierul audio MP3 local
                       FOREIGN KEY (artist_id) REFERENCES Artists(artist_id) ON DELETE CASCADE, -- Dacă un artist este șters, șterge și melodiile sale
                       FOREIGN KEY (album_id) REFERENCES Albums(album_id) ON DELETE SET NULL, -- Dacă un album este șters, melodia rămâne, dar legătura la album devine NULL
                       CONSTRAINT uc_song_title_artist UNIQUE (title, artist_id) -- <<--- ADAUGĂ ACEASTĂ LINIE!
);

-- Tabelul pentru playlist-uri
CREATE TABLE Playlists (
                           playlist_id SERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL, -- <-- Aici s-a schimbat
                           user_id INT NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Tabelul de legătură pentru melodiile din playlist-uri (multe-la-multe)
CREATE TABLE PlaylistSongs (
                               playlist_id INT NOT NULL, -- ID-ul playlist-ului
                               song_id INT NOT NULL, -- ID-ul melodiei
                               order_in_playlist INT NOT NULL, -- Ordinea melodiei în playlist (pentru a menține ordinea)
                               PRIMARY KEY (playlist_id, song_id), -- Cheie primară compusă pentru a asigura unicitatea unei melodii într-un playlist
                               FOREIGN KEY (playlist_id) REFERENCES Playlists(playlist_id) ON DELETE CASCADE, -- Dacă un playlist este șters, șterge și intrările sale din acest tabel
                               FOREIGN KEY (song_id) REFERENCES Songs(song_id) ON DELETE CASCADE -- Dacă o melodie este ștearsă, o elimină din toate playlist-urile
);

-- CREATE INDEXES (Opțional, dar Recomandat pentru Performanță la Căutări Frecvente)
-- Indecșii ajută la accelerarea operațiilor de căutare și filtrare.

CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_songs_title ON Songs(title);
CREATE INDEX idx_artists_name ON Artists(artist_name);
CREATE INDEX idx_albums_name ON Albums(album_name);
CREATE INDEX idx_playlists_user_id ON Playlists(user_id);
CREATE INDEX idx_playlistsongs_playlist_id ON PlaylistSongs(playlist_id);
CREATE INDEX idx_playlistsongs_song_id ON PlaylistSongs(song_id);
CREATE INDEX idx_songs_title_artist ON Songs(title, artist_id);

-- First, insert Billie Eilish as an artist
INSERT INTO Artists (artist_name) 
VALUES ('Billie Eilish')
ON CONFLICT (artist_name) DO NOTHING
RETURNING artist_id;

-- Insert the album
INSERT INTO Albums (album_name, artist_id, release_year)
SELECT 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?', 
       (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
       2019
ON CONFLICT (album_name, artist_id) DO NOTHING
RETURNING album_id;

-- Insert all the songs
INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'bad guy',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '3 minutes 14 seconds',
    'src/main/resources/music/bad guy.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'xanny',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '4 minutes',
    'src/main/resources/music/xanny.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'you should see me in a crown',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '3 minutes',
    'src/main/resources/music/you should see me in a crown.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'all the good girls go to hell',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '2 minutes 48 seconds',
    'src/main/resources/music/all the good girls go to hell.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'wish you were gay',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '3 minutes 36 seconds',
    'src/main/resources/music/Billie Eilish - wish you were gay (Official Audio).mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    '8',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '2 minutes 36 seconds',
    'src/main/resources/music/8.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'my strange addiction',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '3 minutes',
    'src/main/resources/music/my strange addiction.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'ilomilo',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '2 minutes 36 seconds',
    'src/main/resources/music/ilomilo.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'listen before i go',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '4 minutes 36 seconds',
    'src/main/resources/music/listen before i go.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'goodbye',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '2 minutes 12 seconds',
    'src/main/resources/music/goodbye.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;

INSERT INTO Songs (title, artist_id, album_id, duration, file_path)
SELECT 
    'i love you',
    (SELECT artist_id FROM Artists WHERE artist_name = 'Billie Eilish'),
    (SELECT album_id FROM Albums WHERE album_name = 'WHEN WE ALL FALL ASLEEP, WHERE DO WE GO?'),
    INTERVAL '4 minutes 36 seconds',
    'src/main/resources/music/i love you.mp3'
ON CONFLICT (title, artist_id) DO NOTHING;