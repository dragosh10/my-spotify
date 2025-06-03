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
import db.SongDAO;
import db.ArtistDAO;
import java.util.List;
import java.sql.SQLException;

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

    public MainPlayer(User user) {
        this.currentUser = user;
        this.songDAO = new SongDAO();
        this.artistDAO = new ArtistDAO();
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

        Button createPlaylistBtn = new Button("Create Playlist");
        createPlaylistBtn.setMaxWidth(Double.MAX_VALUE);

        ListView<String> playlistList = new ListView<>();
        playlistList.setPrefHeight(400);
        playlistList.setStyle("-fx-background-color: #282828;");

        sidebar.getChildren().addAll(libraryLabel, createPlaylistBtn, playlistList);
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
        try {
            songListView.getItems().clear();
            songListView.getItems().addAll(songDAO.getAllSongs());
        } catch (Exception e) {
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
        }

        try {
            Media media = new Media(song.getFilePath());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volumeSlider.getValue());
            mediaPlayer.play();
            updateSongDisplay(song);
        } catch (Exception e) {
            showError("Error playing song: " + e.getMessage());
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

