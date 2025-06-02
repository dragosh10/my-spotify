-- Insert test user if not exists
INSERT INTO users (username, password)
SELECT 'testuser', 'testpass'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- Insert test song if not exists
INSERT INTO songs (title, artist, album, file_path, duration)
SELECT 'Test Song', 'Test Artist', 'Test Album', 'file:///path/to/test/song.mp3', 180
WHERE NOT EXISTS (SELECT 1 FROM songs WHERE title = 'Test Song'); 