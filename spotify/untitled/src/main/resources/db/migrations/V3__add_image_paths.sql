-- Add image_path columns
ALTER TABLE Albums
ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) DEFAULT '/images/default-album.jpg';

ALTER TABLE Artists
ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) DEFAULT '/images/default-artist.jpg';
