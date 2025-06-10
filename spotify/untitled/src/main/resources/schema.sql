-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create artists table
CREATE TABLE IF NOT EXISTS artists (
    artist_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create albums table
CREATE TABLE IF NOT EXISTS albums (
    album_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    artist_id INTEGER REFERENCES artists(artist_id),
    release_year INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create songs table
CREATE TABLE IF NOT EXISTS songs (
    song_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    artist_id INTEGER REFERENCES artists(artist_id),
    album_id INTEGER REFERENCES albums(album_id),
    duration INTEGER NOT NULL, -- Duration in seconds
    file_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(title, artist_id) -- Prevent duplicate songs by the same artist
);

-- Create playlists table
CREATE TABLE IF NOT EXISTS playlists (
    playlist_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(user_id),
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create PlaylistSongs table (junction table for playlists and songs)
CREATE TABLE IF NOT EXISTS PlaylistSongs (
    playlist_id INTEGER REFERENCES playlists(playlist_id),
    song_id INTEGER REFERENCES songs(song_id),
    order_in_playlist INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (playlist_id, song_id)
); 