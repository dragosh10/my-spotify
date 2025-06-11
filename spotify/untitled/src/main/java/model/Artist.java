package model;

public class Artist {
    private int artistId;
    private String name;
    private String imagePath;

    public Artist(String name) {
        this.name = name;
    }

    public Artist(int artistId, String name) {
        this.artistId = artistId;
        this.name = name;
    }

    public Artist(int artistId, String name, String imagePath) {
        this.artistId = artistId;
        this.name = name;
        this.imagePath = imagePath;
    }

    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath != null ? "/images/" + imagePath : "/images/default-artist.jpg";
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return "Artist{" +
                "artistId=" + artistId +
                ", name='" + name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}