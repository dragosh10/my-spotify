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
import db.SongDAO;
import db.ArtistDAO;
import db.PlaylistDAO;
import db.PlaylistSongsDAO;
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
    private Label songTitleLabel;
    private Label artistNameLabel;
    private ProgressBar progressBar;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private ListView<String> playlistList;
    private Map<String, List<Song>> playlists = new HashMap<>();

    public MainPlayer(User user) {
        this.currentUser = user;
        this.songDAO = new SongDAO();
        this.artistDAO = new ArtistDAO();
        this.playlistDAO = new PlaylistDAO();
        this.playlistSongsDAO = new PlaylistSongsDAO();
        this.playlistList = new ListView<>();
        loadUserPlaylists();
    }

    private void loadUserPlaylists() {
        System.out.println("Attempting to load playlists for user: " + currentUser.getUsername() + " (ID: " + currentUser.getUserId() + ")");
        List<Playlist> userPlaylists = playlistDAO.getPlaylistsByUser(currentUser.getUserId());
        System.out.println("Found " + userPlaylists.size() + " playlists in database.");

        for (Playlist playlist : userPlaylists) {
            System.out.println("  Loading songs for playlist: " + playlist.getName() + " (ID: " + playlist.getPlaylistId() + ")");
            List<Song> songs = playlistSongsDAO.getSongsInPlaylist(playlist.getPlaylistId());
            System.out.println("    Found " + songs.size() + " songs for playlist " + playlist.getName());
            playlists.put(playlist.getName(), songs);
            playlistList.getItems().add(playlist.getName());
            System.out.println("    Playlist " + playlist.getName() + " added to UI and in-memory map.");
        }
        System.out.println("Finished loading user playlists.");
        playlistList.setPrefHeight(400);
        playlistList.setStyle("-fx-background-color: #282828;");
        playlistList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Show selected playlist's songs
                List<Song> playlistSongs = playlists.get(newVal);
                if (playlistSongs != null) {
                    songListView.getItems().clear();
                    songListView.getItems().addAll(playlistSongs);
                }
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MySpotify - Player");

        // Create the main layout
        BorderPane mainLayout = new BorderPane();

        // Left sidebar for playlists
        VBox leftSidebar = createLeftSidebar();
        mainLayout.setLeft(leftSidebar);

        // Center content for song list
        VBox centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        // Bottom player controls
        HBox playerControls = createPlayerControls();
        mainLayout.setBottom(playerControls);

        // Load songs from database
        loadSongs();

        // Create the scene
        Scene scene = new Scene(mainLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
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
                            // Update UI
                            playlists.put(name, selectedSongs);
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
        searchField.setPromptText("Search songs...");
        searchField.setPrefWidth(300);
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchSongs());
        searchBox.getChildren().addAll(searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Song list
        songListView = new ListView<>();
        songListView.setPrefHeight(400);
        songListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        setupSongListView();

        center.getChildren().addAll(searchBox, songListView);
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