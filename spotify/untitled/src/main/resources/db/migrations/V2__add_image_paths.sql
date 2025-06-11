-- Add image_path column to Albums table
ALTER TABLE Albums ADD COLUMN IF NOT EXISTS image_path VARCHAR(255);

-- Add image_path column to Artists table
ALTER TABLE Artists ADD COLUMN IF NOT EXISTS image_path VARCHAR(255);

-- Update existing albums with default image paths
UPDATE Albums SET image_path = '/images/default-album.jpg' WHERE image_path IS NULL;

-- Update existing artists with default image paths
UPDATE Artists SET image_path = '/images/default-artist.jpg' WHERE image_path IS NULL;
