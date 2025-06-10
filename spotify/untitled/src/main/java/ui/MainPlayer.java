package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import model.User;
import model.Song;
import model.Artist;
import model.Playlist;
import model.Album;
import db.SongDAO;
import db.ArtistDAO;
import db.PlaylistDAO;
import db.PlaylistSongsDAO;
import db.AlbumDAO;
import java.util.List;
//import java.sql.SQLException;
//import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class MainPlayer extends Application {
    private User currentUser;
    private MediaPlayer mediaPlayer;
    private ListView<Song> songListView;
    private ListView<Album> albumListView;
    private ListView<Artist> artistListView;
    private Label currentSongLabel;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private Button previousButton;
    private TextField searchField;
    private SongDAO songDAO;
    private ArtistDAO artistDAO;
    private PlaylistDAO playlistDAO;
    private PlaylistSongsDAO playlistSongsDAO;
    private AlbumDAO albumDAO;
    private Label songTitleLabel;
    private Label artistNameLabel;
    private ProgressBar progressBar;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private ListView<String> playlistList;
    private Map<String, Playlist> playlists = new HashMap<>();
    private TabPane tabPane;
    private List<Song> allSongs = new ArrayList<>();

    public MainPlayer(User user) {
        this.currentUser = user;
        this.songDAO = new SongDAO();
        this.artistDAO = new ArtistDAO();
        this.playlistDAO = new PlaylistDAO();
        this.playlistSongsDAO = new PlaylistSongsDAO();
        this.albumDAO = new AlbumDAO();
        this.songListView = new ListView<>();
        this.playlistList = new ListView<>();
        this.albumListView = new ListView<>();
        this.artistListView = new ListView<>();
    }

    private void loadUserPlaylists() {
        System.out.println("Attempting to load playlists for user: " + currentUser.getUsername() + " (ID: " + currentUser.getUserId() + ")");
        List<Playlist> userPlaylists = playlistDAO.getPlaylistsByUser(currentUser.getUserId());
        System.out.println("Found " + userPlaylists.size() + " playlists in database.");

        playlistList.getItems().clear();
        playlists.clear(); // Clear the existing map

        for (Playlist playlist : userPlaylists) {
            System.out.println("  Loading playlist: " + playlist.getName() + " (ID: " + playlist.getPlaylistId() + ")");
            // Get the songs for this playlist
            List<Song> playlistSongs = playlistSongsDAO.getSongsInPlaylist(playlist.getPlaylistId());
            System.out.println("  Found " + playlistSongs.size() + " songs in playlist " + playlist.getName());
            playlist.setSongs(playlistSongs);
            
            playlistList.getItems().add(playlist.getName());
            playlists.put(playlist.getName(), playlist);
        }
        
        System.out.println("Finished loading user playlists.");
        playlistList.setPrefHeight(400);
        playlistList.setStyle("-fx-background-color: #282828;");
        
        playlistList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Playlist selectedPlaylist = playlists.get(newVal);
                if (selectedPlaylist != null) {
                    System.out.println("Selected playlist: " + selectedPlaylist.getName() + " (ID: " + selectedPlaylist.getPlaylistId() + ")");
                    // Update songs view
                    songListView.getItems().clear();
                    songListView.getItems().addAll(selectedPlaylist.getSongs());
                } else {
                    System.out.println("Warning: Could not find playlist with name: " + newVal);
                }
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MySpotify - Player");

        // Create the main layout
        BorderPane mainLayout = new BorderPane();

        // Create all UI components first
        VBox centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        VBox leftSidebar = createLeftSidebar();
        mainLayout.setLeft(leftSidebar);

        HBox playerControls = createPlayerControls();
        mainLayout.setBottom(playerControls);

        // Create the scene
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load data after UI is set up
        loadAllData();
    }

    private VBox createLeftSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #282828;");

        Label libraryLabel = new Label("Your Library");
        libraryLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        // Home button
        Button homeButton = new Button("Home");
        homeButton.getStyleClass().add("control-button");
        homeButton.setOnAction(e -> {
            loadSongs(); // Reload all songs
            playlistList.getSelectionModel().clearSelection(); // Deselect current playlist
        });

        // Create playlist button
        Button createPlaylistButton = new Button("Create Playlist");
        createPlaylistButton.getStyleClass().add("control-button");
        createPlaylistButton.setOnAction(e -> {
            // Create dialog for playlist name
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Create Playlist");
            nameDialog.setHeaderText("Enter playlist name");
            nameDialog.setContentText("Name:");

            nameDialog.showAndWait().ifPresent(name -> {
                // Create dialog for song selection
                Dialog<List<Song>> songDialog = new Dialog<>();
                songDialog.setTitle("Select Songs");
                songDialog.setHeaderText("Choose songs for playlist: " + name);

                // Create a ListView for song selection
                ListView<Song> songSelectionList = new ListView<>();
                songSelectionList.getItems().addAll(songListView.getItems());
                songSelectionList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                songSelectionList.setCellFactory(lv -> new ListCell<>() {
                    @Override
                    protected void updateItem(Song song, boolean empty) {
                        super.updateItem(song, empty);
                        if (empty || song == null) {
                            setText(null);
                        } else {
                            try {
                                Artist artist = artistDAO.getArtistById(song.getArtistId());
                                String artistName = artist != null ? artist.getName() : "Unknown Artist";
                                setText(song.getTitle() + " - " + artistName);
                            } catch (Exception e) {
                                setText(song.getTitle() + " - Unknown Artist");
                            }
                        }
                    }
                });

                // Add buttons
                ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
                songDialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

                // Set the content
                songDialog.getDialogPane().setContent(songSelectionList);

                // Convert the result to a list of selected songs
                songDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == createButtonType) {
                        return new ArrayList<>(songSelectionList.getSelectionModel().getSelectedItems());
                    }
                    return null;
                });

                // Show the dialog and handle the result
                songDialog.showAndWait().ifPresent(selectedSongs -> {
                    if (!selectedSongs.isEmpty()) {
                        // Create playlist in database
                        Playlist playlist = playlistDAO.createPlaylist(name, currentUser.getUserId());
                        if (playlist != null) {
                            // Add songs to playlist
                            for (int i = 0; i < selectedSongs.size(); i++) {
                                Song song = selectedSongs.get(i);
                                playlistSongsDAO.addSongToPlaylist(playlist.getPlaylistId(), song.getSongId(), i + 1);
                            }
                            // Set the songs in the playlist object
                            playlist.setSongs(selectedSongs);
                            // Update UI and local state
                            playlists.put(name, playlist);
                            playlistList.getItems().add(name);

                        }
                    }
                });
            });
        });

        sidebar.getChildren().addAll(libraryLabel, homeButton, createPlaylistButton, playlistList);
        return sidebar;
    }

    private VBox createCenterContent() {
        VBox center = new VBox(10);
        center.setPadding(new Insets(10));

        // Search bar
        HBox searchBox = new HBox(10);
        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(300);
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchSongs());
        searchBox.getChildren().addAll(searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Create TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Songs Tab
        Tab songsTab = new Tab("Songs");
        songListView.setPrefHeight(400);
        songListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupSongListView();
        songsTab.setContent(songListView);

        // Albums Tab
        Tab albumsTab = new Tab("Albums");
        albumListView.setPrefHeight(400);
        setupAlbumListView();
        albumsTab.setContent(albumListView);

        // Artists Tab
        Tab artistsTab = new Tab("Artists");
        artistListView.setPrefHeight(400);
        setupArtistListView();
        artistsTab.setContent(artistListView);

        tabPane.getTabs().addAll(songsTab, albumsTab, artistsTab);

        center.getChildren().addAll(searchBox, tabPane);
        return center;
    }

    private HBox createPlayerControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #282828;");

        // Playback controls
        previousButton = new Button("⏮");
        playButton = new Button("▶");
        pauseButton = new Button("⏸");
        nextButton = new Button("⏭");

        // Current song info
        currentSongLabel = new Label("No song selected");
        currentSongLabel.setStyle("-fx-text-fill: white;");

        // Progress slider
        progressSlider = new Slider();
        progressSlider.setPrefWidth(300);

        // Volume control
        volumeSlider = new Slider(0, 1, 0.5);
        volumeSlider.setPrefWidth(100);

        // Add all controls
        controls.getChildren().addAll(
                previousButton,
                playButton,
                pauseButton,
                nextButton,
                currentSongLabel,
                progressSlider,
                new Label("🔊"),
                volumeSlider
        );

        // Add event handlers
        setupEventHandlers();

        return controls;
    }

    private void setupEventHandlers() {
        playButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.play();
            }
        });

        pauseButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        songListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                playSelectedSong(newVal);
            }
        });
    }

    private void loadSongs() {
        System.out.println("Attempting to load songs...");
        try {
            songListView.getItems().clear();
            List<Song> songs = songDAO.getAllSongs();
            System.out.println("Found " + songs.size() + " songs.");
            songListView.getItems().addAll(songs);
        } catch (Exception e) {
            System.err.println("Error loading songs: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading songs", e.getMessage());
        }
    }

    private void searchSongs() {
        try {
            String query = searchField.getText().trim();
            songListView.getItems().clear();
            songListView.getItems().addAll(songDAO.searchSongs(query));
        } catch (Exception e) {
            showError("Error searching songs", e.getMessage());
        }
    }

    private void playSelectedSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose(); // Dispose the old media player to release resources
        }

        try {
            // Convert the relative file path to an absolute URI
            String absolutePath = new java.io.File(song.getFilePath()).getAbsolutePath();
            Media media = new Media(new java.io.File(absolutePath).toURI().toString());

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volumeSlider.getValue());
            mediaPlayer.play();
            updateSongDisplay(song);
        } catch (Exception e) {
            showError("Error playing song", "Could not play song: " + e.getMessage() + "\nFile path: " + song.getFilePath());
            e.printStackTrace();
        }
    }

    private void updateSongDisplay(Song song) {
        if (song != null) {
            try {
                Artist artist = artistDAO.getArtistById(song.getArtistId());
                String artistName = artist != null ? artist.getName() : "Unknown Artist";
                currentSongLabel.setText(song.getTitle() + " - " + artistName);
            } catch (Exception e) {
                currentSongLabel.setText(song.getTitle() + " - Unknown Artist");
            }
        } else {
            currentSongLabel.setText("No song selected");
        }
    }

    private void setupSongListView() {
        songListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    try {
                        Artist artist = artistDAO.getArtistById(song.getArtistId());
                        String artistName = artist != null ? artist.getName() : "Unknown Artist";
                        setText(song.getTitle() + " - " + artistName);
                    } catch (Exception e) {
                        setText(song.getTitle() + " - Unknown Artist");
                    }
                }
            }
        });
    }

    private void setupAlbumListView() {
        albumListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    setText(album.getTitle());
                }
            }
        });

        albumListView.setOnMouseClicked(event -> {
            Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
            if (selectedAlbum != null) {
                System.out.println("Selected album: " + selectedAlbum.getTitle() + " (ID: " + selectedAlbum.getAlbumId() + ")");
                List<Song> albumSongs = songDAO.getAllSongs().stream()
                    .filter(song -> song.getAlbumId() != null && song.getAlbumId().equals(selectedAlbum.getAlbumId()))
                    .toList();
                System.out.println("Found " + albumSongs.size() + " songs for album");
                songListView.getItems().clear();
                songListView.getItems().addAll(albumSongs);
                tabPane.getSelectionModel().select(0); // Switch to Songs tab
            }
        });
    }

    private void setupArtistListView() {
        artistListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Artist artist, boolean empty) {
                super.updateItem(artist, empty);
                if (empty || artist == null) {
                    setText(null);
                } else {
                    setText(artist.getName());
                }
            }
        });

        artistListView.setOnMouseClicked(event -> {
            Artist selectedArtist = artistListView.getSelectionModel().getSelectedItem();
            if (selectedArtist != null) {
                // Load albums for the selected artist
                List<Album> artistAlbums = albumDAO.getAlbumsByArtist(selectedArtist.getArtistId());
                albumListView.getItems().clear();
                albumListView.getItems().addAll(artistAlbums);
                tabPane.getSelectionModel().select(1); // Switch to Albums tab
            }
        });
    }

    private void loadAllData() {
        // Load songs
        System.out.println("Loading songs...");
        allSongs = songDAO.getAllSongs();
        songListView.getItems().clear();
        songListView.getItems().addAll(allSongs);

        // Load albums
        System.out.println("Loading albums...");
        List<Album> albums = new ArrayList<>();
        // Get all albums directly from the database
        allSongs.stream()
            .filter(song -> song.getAlbumId() != null)
            .map(Song::getAlbumId)
            .distinct()
            .forEach(albumId -> {
                Album album = albumDAO.getAlbumById(albumId);
                if (album != null) {
                    System.out.println("Found album: " + album.getAlbumName());
                    albums.add(album);
                }
            });
        albumListView.getItems().clear();
        albumListView.getItems().addAll(albums);

        // Load artists
        System.out.println("Loading artists...");
        List<Artist> artists = artistDAO.getAllArtists();
        artistListView.getItems().clear();
        artistListView.getItems().addAll(artists);

        // Load playlists
        loadUserPlaylists();
    }

    private void showError(String message) {
        showError("Error", message);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}