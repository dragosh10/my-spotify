package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.User;
import model.Song;
import model.Artist;
import model.Playlist;
import model.Album;
import db.ArtistDAO;
import db.PlaylistDAO;
import db.PlaylistSongsDAO;
import db.AlbumDAO;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MainPlayer extends Application {
    private User currentUser;
    private MediaPlayer mediaPlayer;
    private StackPane mainContentArea;
    private Label currentSongLabel;
    private Slider volumeSlider;
    private Button playButton;
    private Button pauseButton;
    private TextField searchField;
    private Button backButton;

    private ArtistDAO artistDAO;
    private PlaylistDAO playlistDAO;
    private PlaylistSongsDAO playlistSongsDAO;
    private AlbumDAO albumDAO;
    private ListView<String> playlistList;
    private Map<String, Playlist> playlists = new HashMap<>();
    private TabPane mainTabPane;

    public MainPlayer(User user) {
        this.currentUser = user;
        this.artistDAO = new ArtistDAO();
        this.playlistDAO = new PlaylistDAO();
        this.playlistSongsDAO = new PlaylistSongsDAO();
        this.albumDAO = new AlbumDAO();
        this.playlistList = new ListView<>();
        this.mainContentArea = new StackPane();
    }

    private void loadUserPlaylists() {
        System.out.println("Attempting to load playlists for user: " + currentUser.getUsername() + " (ID: "
                + currentUser.getUserId() + ")");
        List<Playlist> userPlaylists = playlistDAO.getPlaylistsByUser(currentUser.getUserId());
        System.out.println("Found " + userPlaylists.size() + " playlists in database.");

        playlistList.getItems().clear();
        playlists.clear();

        for (Playlist playlist : userPlaylists) {
            System.out.println("  Loading playlist: " + playlist.getName() + " (ID: " + playlist.getPlaylistId() + ")");
            List<Song> playlistSongs = playlistSongsDAO.getSongsInPlaylist(playlist.getPlaylistId());
            System.out.println("  Found " + playlistSongs.size() + " songs in playlist " + playlist.getName());
            playlist.setSongs(playlistSongs);

            playlistList.getItems().add(playlist.getName());
            playlists.put(playlist.getName(), playlist);
        }

        System.out.println("Finished loading user playlists.");
        playlistList.getStyleClass().add("playlist-list");

        playlistList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Playlist selectedPlaylist = playlists.get(newVal);
                if (selectedPlaylist != null) {
                    showPlaylistSongsDialog(selectedPlaylist);
                } else {
                    System.out.println("Warning: Could not find playlist with name: " + newVal);
                }
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MySpotify - Player");

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-player-root");

        mainContentArea.getChildren().add(createCenterContent());
        mainLayout.setCenter(mainContentArea);

        VBox leftSidebar = createLeftSidebar();
        mainLayout.setLeft(leftSidebar);

        // Create shared player controls
        sharedPlayerControls = createPlayerControls();
        mainLayout.setBottom(sharedPlayerControls);

        Scene scene = new Scene(mainLayout, 1200, 800);
        String cssPath = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadAllData();
    }

    private VBox createLeftSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");

        Label libraryLabel = new Label("Your Library");
        libraryLabel.getStyleClass().add("library-label");

        Button homeButton = new Button("🏠 Home");
        homeButton.getStyleClass().add("sidebar-button");
        homeButton.setOnAction(e -> {
            loadAllData();
            playlistList.getSelectionModel().clearSelection();
        });

        Button createPlaylistButton = new Button("➕ Create Playlist");
        createPlaylistButton.getStyleClass().add("create-playlist-button");
        createPlaylistButton.setOnAction(e -> createNewPlaylist());

        sidebar.getChildren().addAll(libraryLabel, homeButton, createPlaylistButton, playlistList);
        return sidebar;
    }

    private VBox createCenterContent() {
        VBox center = new VBox(20);
        center.getStyleClass().add("content-area");

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.getStyleClass().add("search-box");
        searchField = new TextField();
        searchField.setPromptText("🔍 Search albums or artists");
        searchField.getStyleClass().add("search-field");
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("search-button");
        searchButton.setOnAction(e -> searchContent());
        searchBox.getChildren().addAll(searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        // Content tabs
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Albums tab with grid
        Tab albumsTab = new Tab("Albums");
        GridPane albumGrid = createAlbumGrid();
        ScrollPane albumScroll = new ScrollPane(albumGrid);
        albumScroll.setFitToWidth(true);
        albumScroll.setFitToHeight(true);
        albumsTab.setContent(albumScroll);

        // Artists tab with grid
        Tab artistsTab = new Tab("Artists");
        GridPane artistGrid = createArtistGrid();
        ScrollPane artistScroll = new ScrollPane(artistGrid);
        artistScroll.setFitToWidth(true);
        artistScroll.setFitToHeight(true);
        artistsTab.setContent(artistScroll);

        mainTabPane.getTabs().addAll(albumsTab, artistsTab);
        center.getChildren().addAll(searchBox, mainTabPane);
        return center;
    }

    private HBox createPlayerControls() {
        HBox controls = new HBox(20);
        controls.getStyleClass().add("player-controls");
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        // Left section - Current song info
        VBox songInfo = new VBox(5);
        currentSongLabel = new Label("No song playing");
        currentSongLabel.getStyleClass().addAll("current-song-label", "text-white");
        songInfo.getChildren().add(currentSongLabel);

        // Center section - Playback controls
        HBox playbackControls = new HBox(10);
        playbackControls.setAlignment(Pos.CENTER);

        playButton = new Button("▶");
        playButton.getStyleClass().addAll("player-button", "control-button");
        playButton.setOnAction(e -> resumePlayback());
        playButton.setDisable(true);  // Initially disabled

        pauseButton = new Button("⏸");
        pauseButton.getStyleClass().addAll("player-button", "control-button");
        pauseButton.setOnAction(e -> pausePlayback());
        pauseButton.setDisable(true);  // Initially disabled

        playbackControls.getChildren().addAll(playButton, pauseButton);

        // Right section - Volume control
        HBox volumeControls = new HBox(10);
        volumeControls.setAlignment(Pos.CENTER_RIGHT);

        Label volumeIcon = new Label("🔊");
        volumeIcon.getStyleClass().add("text-white");

        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setPrefWidth(100);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });

        volumeControls.getChildren().addAll(volumeIcon, volumeSlider);

        // Add all sections to the controls with spacers
        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        
        controls.getChildren().addAll(
                songInfo,
                leftSpacer,
                playbackControls,
                rightSpacer,
                volumeControls);

        return controls;
    }

    private GridPane createAlbumGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid-view");
        grid.setVgap(20);
        grid.setHgap(20);

        List<Album> albums = albumDAO.getAllAlbums();
        int column = 0;
        int row = 0;
        int maxColumns = 4;

        for (Album album : albums) {
            VBox albumBox = createAlbumBox(album);
            grid.add(albumBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        return grid;
    }

    private GridPane createArtistGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("grid-view");
        grid.setVgap(20);
        grid.setHgap(20);

        List<Artist> artists = artistDAO.getAllArtists();
        int column = 0;
        int row = 0;
        int maxColumns = 4;

        for (Artist artist : artists) {
            VBox artistBox = createArtistBox(artist);
            grid.add(artistBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        return grid;
    }

    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResource(path).toExternalForm());
        } catch (Exception e) {
            System.err.println("Error loading image from path: " + path);
            System.err.println(e.getMessage());
            try {
                return new Image(getClass().getResource("/images/default-album.jpg").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Could not load default image: " + ex.getMessage());
                return null;
            }
        }
    }

    /*
     * private VBox createAlbumBox(Album album) {
     * VBox box = new VBox(10);
     * box.getStyleClass().add("grid-cell");
     * box.setAlignment(Pos.CENTER);
     * 
     * ImageView imageView = new ImageView(loadImage(album.getImagePath()));
     * imageView.setFitWidth(168);
     * imageView.setFitHeight(168);
     * imageView.getStyleClass().add("grid-cell-image");
     * 
     * Label titleLabel = new Label(album.getTitle());
     * titleLabel.getStyleClass().add("grid-cell-title");
     * 
     * Artist artist = artistDAO.getArtistById(album.getArtistId());
     * Label artistLabel = new Label(artist != null ? artist.getName() :
     * "Unknown Artist");
     * artistLabel.getStyleClass().add("grid-cell-subtitle");
     * 
     * box.getChildren().addAll(imageView, titleLabel, artistLabel);
     * box.setOnMouseClicked(event -> {
     * System.out.println("Opening album: " + album.getTitle() + " (ID: " +
     * album.getAlbumId() + ")");
     * showAlbumContent(album);
     * });
     * 
     * return box;
     * }
     */
    private VBox createAlbumContent(Album album) {
        VBox albumView = new VBox(20);
        albumView.getStyleClass().add("album-content");
        albumView.setPadding(new Insets(20));

        // Back button
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showMainView());

        // Album header
        HBox header = new HBox(20);
        header.getStyleClass().add("album-header");

        ImageView albumCover = new ImageView(loadImage(album.getImagePath()));
        albumCover.setFitWidth(232);
        albumCover.setFitHeight(232);
        albumCover.getStyleClass().add("album-cover");

        VBox albumInfo = new VBox(10);
        albumInfo.getStyleClass().add("album-info");

        Label titleLabel = new Label(album.getTitle());
        titleLabel.getStyleClass().add("album-title");

        Artist artist = artistDAO.getArtistById(album.getArtistId());
        Label artistLabel = new Label(artist != null ? artist.getName() : "Unknown Artist");
        artistLabel.getStyleClass().add("album-artist");

        albumInfo.getChildren().addAll(titleLabel, artistLabel);
        header.getChildren().addAll(albumCover, albumInfo);

        // Songs list
        VBox songsList = new VBox(2);
        songsList.getStyleClass().add("songs-list");

        List<Song> songs = albumDAO.getSongsInAlbum(album.getAlbumId());
        int songNumber = 1;
        for (Song song : songs) {
            HBox songRow = createSongRow(song, songNumber++);
            songsList.getChildren().add(songRow);
        }

        albumView.getChildren().addAll(backButton, header, songsList);
        return albumView;
    }

    private VBox createAlbumBox(Album album) {
        VBox box = new VBox(10);
        box.getStyleClass().add("grid-cell");
        box.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView(loadImage(album.getImagePath()));
        imageView.setFitWidth(168);
        imageView.setFitHeight(168);
        imageView.getStyleClass().add("grid-cell-image");

        Label titleLabel = new Label(album.getTitle());
        titleLabel.getStyleClass().add("grid-cell-title");

        Artist artist = artistDAO.getArtistById(album.getArtistId());
        Label artistLabel = new Label(artist != null ? artist.getName() : "Unknown Artist");
        artistLabel.getStyleClass().add("grid-cell-subtitle");

        box.getChildren().addAll(imageView, titleLabel, artistLabel);
        box.setOnMouseClicked(event -> {
            System.out.println("Album clicked: " + album.getTitle() + " (ID: " + album.getAlbumId() + ")");
            showAlbumContent(album);
        });

        return box;
    }

    private VBox createArtistBox(Artist artist) {
        VBox box = new VBox(10);
        box.getStyleClass().add("grid-cell");
        box.setAlignment(Pos.CENTER);

        ImageView imageView = new ImageView(loadImage(artist.getImagePath()));
        imageView.setFitWidth(168);
        imageView.setFitHeight(168);
        imageView.getStyleClass().add("grid-cell-image");

        Label nameLabel = new Label(artist.getName());
        nameLabel.getStyleClass().add("grid-cell-title");

        box.getChildren().addAll(imageView, nameLabel);
        box.setOnMouseClicked(event -> showArtistContent(artist));

        return box;
    }

    private HBox sharedPlayerControls;

    private void showAlbumContent(Album album) {
        // Print debug information
        System.out.println("Showing content for album: " + album.getTitle() + " (ID: " + album.getAlbumId() + ")");
        
        VBox albumView = new VBox(20);
        albumView.getStyleClass().add("album-content");
        albumView.setPadding(new Insets(20));

        // Back button
        Button backButton = new Button("← Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showMainView());

        // Album header
        HBox header = new HBox(20);
        header.getStyleClass().add("album-header");
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView albumCover = new ImageView(loadImage(album.getImagePath()));
        albumCover.setFitWidth(232);
        albumCover.setFitHeight(232);
        albumCover.getStyleClass().add("album-cover");

        VBox albumInfo = new VBox(10);
        albumInfo.getStyleClass().add("album-info");

        Label albumLabel = new Label("ALBUM");
        albumLabel.getStyleClass().add("album-label");

        Label titleLabel = new Label(album.getTitle());
        titleLabel.getStyleClass().add("album-title");

        // Artist info
    Artist artist = artistDAO.getArtistById(album.getArtistId());
    if (artist == null) {
        System.err.println("Could not find artist with ID: " + album.getArtistId());
        // Try to reload the artist
        artist = artistDAO.getArtistById(album.getArtistId());
    }
    Label artistLabel = new Label(artist != null ? artist.getName() : "Unknown Artist");
    artistLabel.getStyleClass().add("album-artist");

        albumInfo.getChildren().addAll(albumLabel, titleLabel, artistLabel);
        header.getChildren().addAll(albumCover, albumInfo);

        // Songs container
        VBox songsContainer = new VBox(10);
        songsContainer.getStyleClass().add("songs-container");

        // Songs header
        HBox songsHeader = new HBox(15);
        songsHeader.getStyleClass().add("songs-header");
        songsHeader.setAlignment(Pos.CENTER_LEFT);

        Label numberHeader = new Label("#");
        Label titleHeader = new Label("TITLE");
        Label durationHeader = new Label("DURATION");

        songsHeader.getChildren().addAll(numberHeader, titleHeader, durationHeader);
        HBox.setHgrow(titleHeader, Priority.ALWAYS);

        // Songs list
        VBox songsList = new VBox(2);
        songsList.getStyleClass().add("songs-list");
        
        // Get songs and print debug information
        List<Song> songs = albumDAO.getSongsInAlbum(album.getAlbumId());
        System.out.println("Found " + songs.size() + " songs for album: " + album.getTitle());
        
        if (songs.isEmpty()) {
            System.out.println("No songs found for album: " + album.getTitle());
            Label noSongsLabel = new Label("No songs in this album");
            noSongsLabel.getStyleClass().add("no-songs-label");
            songsList.getChildren().add(noSongsLabel);
        } else {
            int songNumber = 1;
            for (Song song : songs) {
                System.out.println("Adding song: " + song.getTitle() + " (ID: " + song.getSongId() + ")");
                HBox songRow = createSongRow(song, songNumber++);
                songsList.getChildren().add(songRow);
            }
        }

        songsContainer.getChildren().addAll(songsHeader, songsList);

        // Create a spacer to push the player controls to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create main layout that includes both content and player
        VBox rootLayout = new VBox(0);
        rootLayout.getStyleClass().add("main-layout");
        
        // Add main content
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(backButton, header, songsContainer, spacer);
        mainContent.setStyle("-fx-background-color: #121212;"); // Dark background
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Set common style for player controls
        sharedPlayerControls.getStyleClass().addAll("player-controls");
        sharedPlayerControls.setPadding(new Insets(10));
        sharedPlayerControls.setStyle("-fx-background-color: #282828;"); // Dark background like Spotify
        
        rootLayout.getChildren().addAll(mainContent, sharedPlayerControls);
        
        switchToView(rootLayout);
    }

    private void showArtistContent(Artist artist) {
        VBox artistView = new VBox();
        artistView.getStyleClass().add("artist-content");

        // Header with artist info
        HBox header = new HBox(20);
        header.getStyleClass().add("artist-header");

        ImageView artistImage = new ImageView(loadImage(artist.getImagePath()));
        artistImage.setFitWidth(232);
        artistImage.setFitHeight(232);
        artistImage.getStyleClass().add("artist-image");

        VBox artistInfo = new VBox(8);
        artistInfo.getStyleClass().add("artist-info");

        Label nameLabel = new Label(artist.getName());
        nameLabel.getStyleClass().add("artist-name");

        artistInfo.getChildren().add(nameLabel);
        header.getChildren().addAll(artistImage, artistInfo);

        // Back button
        backButton = new Button("← Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showMainView());

        // Albums grid
        GridPane albumsGrid = new GridPane();
        albumsGrid.getStyleClass().add("grid-view");
        albumsGrid.setVgap(20);
        albumsGrid.setHgap(20);

        List<Album> artistAlbums = albumDAO.getAlbumsByArtist(artist.getArtistId());
        int column = 0;
        int row = 0;
        int maxColumns = 4;

        for (Album album : artistAlbums) {
            VBox albumBox = createAlbumBox(album);
            albumsGrid.add(albumBox, column, row);

            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        ScrollPane scrollPane = new ScrollPane(albumsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        // Create main layout that includes both content and player
        VBox rootLayout = new VBox(0);
        rootLayout.getStyleClass().add("main-layout");
        
        // Add main content
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(backButton, header, scrollPane);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Set common style for player controls
        sharedPlayerControls.getStyleClass().addAll("player-controls");
        sharedPlayerControls.setPadding(new Insets(10));
        sharedPlayerControls.setStyle("-fx-background-color: #282828;"); // Dark background like Spotify
        
        rootLayout.getChildren().addAll(mainContent, sharedPlayerControls);
        
        switchToView(rootLayout);
    }

    private HBox createSongRow(Song song, int number) {
    HBox row = new HBox(15);
    row.getStyleClass().add("song-row");
    row.setAlignment(Pos.CENTER_LEFT);

    Label numberLabel = new Label(String.format("%d", number));
    numberLabel.getStyleClass().add("song-number");

    Label titleLabel = new Label(song.getTitle());
    titleLabel.getStyleClass().add("song-title");
    HBox.setHgrow(titleLabel, Priority.ALWAYS);

    // Get duration from the actual media file
    javafx.util.Duration fxDuration;
    try {
        // Fix the file path to point to the correct location in resources
        String resourcePath = "/music/" + song.getFilePath().substring(song.getFilePath().lastIndexOf('/') + 1);
        String uri = getClass().getResource(resourcePath).toURI().toString();
        Media media = new Media(uri);
        MediaPlayer tempPlayer = new MediaPlayer(media);
        fxDuration = media.getDuration();
        tempPlayer.dispose(); // Clean up the temporary player
        System.out.println("Successfully got duration for: " + song.getTitle() + " from: " + resourcePath);
    } catch (Exception e) {
        System.err.println("Error getting duration for " + song.getTitle() + ": " + e.getMessage());
        // Fallback to the stored duration if media file can't be read
        fxDuration = song.getJavaFXDuration();
    }
    
    String durationStr = String.format("%d:%02d",
            (int) fxDuration.toMinutes(),
            (int) (fxDuration.toSeconds() % 60));
    Label durationLabel = new Label(durationStr);
    durationLabel.getStyleClass().add("song-duration");

    row.getChildren().addAll(numberLabel, titleLabel, durationLabel);

    row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 1) { // Changed to single click
            playSelectedSong(song);
        }
    });

    return row;
}
     private void switchToView(Node view) {
        Platform.runLater(() -> {
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(view);
        });
    }

    private void showMainView() {
        Platform.runLater(() -> {
            // Create main layout that includes both content and player
            VBox rootLayout = new VBox(0);
            rootLayout.getStyleClass().add("main-layout");
            
            // Create content area
            VBox centerContent = new VBox(20);
            centerContent.getStyleClass().add("content-area");
            VBox.setVgrow(centerContent, Priority.ALWAYS);

            // Search bar
            HBox searchBox = new HBox(10);
            searchBox.getStyleClass().add("search-box");
            searchField = new TextField();
            searchField.setPromptText("🔍 Search albums or artists");
            searchField.getStyleClass().add("search-field");
            Button searchButton = new Button("Search");
            searchButton.getStyleClass().add("search-button");
            searchButton.setOnAction(e -> searchContent());
            searchBox.getChildren().addAll(searchField, searchButton);
            searchBox.setAlignment(Pos.CENTER_LEFT);

            // Content tabs
            mainTabPane = new TabPane();
            mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Albums tab with grid
            Tab albumsTab = new Tab("Albums");
            GridPane albumGrid = createAlbumGrid();
            ScrollPane albumScroll = new ScrollPane(albumGrid);
            albumScroll.setFitToWidth(true);
            albumScroll.setFitToHeight(true);
            albumsTab.setContent(albumScroll);

            // Artists tab with grid
            Tab artistsTab = new Tab("Artists");
            GridPane artistGrid = createArtistGrid();
            ScrollPane artistScroll = new ScrollPane(artistGrid);
            artistScroll.setFitToWidth(true);
            artistScroll.setFitToHeight(true);
            artistsTab.setContent(artistScroll);

            mainTabPane.getTabs().addAll(albumsTab, artistsTab);
            centerContent.getChildren().addAll(searchBox, mainTabPane);

            // Add content and player controls to root layout
            rootLayout.getChildren().addAll(centerContent);
            
            // Set common style for player controls
            sharedPlayerControls.getStyleClass().addAll("player-controls");
            sharedPlayerControls.setPadding(new Insets(10));
            sharedPlayerControls.setStyle("-fx-background-color: #282828;"); // Dark background like Spotify
            
            rootLayout.getChildren().add(sharedPlayerControls);

            // Update the main content area
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(rootLayout);

            // Reload playlists
            loadUserPlaylists();
        });
    }

    private void loadAllData() {
        showMainView();
        loadUserPlaylists();
    }

    private List<Album> getAllAlbums() {
        return albumDAO.getAllAlbums();
    }

    private void showAlbumSongsDialog(Album album) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(album.getTitle());
        dialog.setHeaderText("Songs in Album");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("album-dialog");

        ListView<Song> songListView = new ListView<>();
        songListView.getItems().addAll(albumDAO.getSongsInAlbum(album.getAlbumId()));
        songListView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(song.getTitle());
                }
            }
        });

        songListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = songListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    playSelectedSong(selectedSong);
                    dialog.close();
                }
            }
        });

        dialogPane.setContent(songListView);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void showArtistAlbumsDialog(Artist artist) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(artist.getName());
        dialog.setHeaderText("Albums by " + artist.getName());

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("artist-dialog");

        ListView<Album> artistAlbumListView = new ListView<>();
        artistAlbumListView.getItems().addAll(albumDAO.getAlbumsByArtist(artist.getArtistId()));
        artistAlbumListView.setCellFactory(lv -> new ListCell<Album>() {
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

        artistAlbumListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Album selectedAlbum = artistAlbumListView.getSelectionModel().getSelectedItem();
                if (selectedAlbum != null) {
                    dialog.close();
                    showAlbumSongsDialog(selectedAlbum);
                }
            }
        });

        dialogPane.setContent(artistAlbumListView);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }

    private void createNewPlaylist() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Playlist");
        dialog.setHeaderText("Enter playlist name");
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Playlist newPlaylist = playlistDAO.createPlaylist(name, currentUser.getUserId());
                if (newPlaylist != null) {
                    showAlbumSelectionDialog(newPlaylist.getPlaylistId(), name);
                }
            }
        });
    }

    private void showAlbumSelectionDialog(int playlistId, String playlistName) {
        Dialog<List<Album>> dialog = new Dialog<>();
        dialog.setTitle("Add Albums to Playlist");
        dialog.setHeaderText("Select albums for " + playlistName);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("album-selection-dialog");

        ListView<Album> albumSelectionView = new ListView<>();
        albumSelectionView.getItems().addAll(getAllAlbums());
        albumSelectionView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dialogPane.setContent(albumSelectionView);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new ArrayList<>(albumSelectionView.getSelectionModel().getSelectedItems());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selectedAlbums -> {
            for (Album album : selectedAlbums) {
                List<Song> albumSongs = albumDAO.getSongsInAlbum(album.getAlbumId());
                int position = 1;
                for (Song song : albumSongs) {
                    playlistSongsDAO.addSongToPlaylist(playlistId, song.getSongId(), position++);
                }
            }
            loadUserPlaylists();
        });
    }

    private void searchContent() {
        String searchTerm = searchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadAllData();
            return;
        }

        // Create search results view
        VBox searchResults = new VBox(20);
        searchResults.getStyleClass().add("content-area");

        // Add back button
        backButton = new Button("← Back");
        backButton.getStyleClass().add("back-button");
        backButton.setOnAction(e -> showMainView());

        // Create tabs for search results
        TabPane searchTabPane = new TabPane();
        searchTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Filter and display albums
        List<Album> filteredAlbums = albumDAO.getAllAlbums().stream()
                .filter(album -> album.getTitle().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        Tab albumsTab = new Tab("Albums");
        GridPane albumGrid = new GridPane();
        albumGrid.getStyleClass().add("grid-view");
        albumGrid.setVgap(20);
        albumGrid.setHgap(20);

        int column = 0;
        int row = 0;
        int maxColumns = 4;

        for (Album album : filteredAlbums) {
            VBox albumBox = createAlbumBox(album);
            albumGrid.add(albumBox, column, row);
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        ScrollPane albumScroll = new ScrollPane(albumGrid);
        albumScroll.setFitToWidth(true);
        albumsTab.setContent(albumScroll);

        // Filter and display artists
        List<Artist> filteredArtists = artistDAO.getAllArtists().stream()
                .filter(artist -> artist.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());

        Tab artistsTab = new Tab("Artists");
        GridPane artistGrid = new GridPane();
        artistGrid.getStyleClass().add("grid-view");
        artistGrid.setVgap(20);
        artistGrid.setHgap(20);

        column = 0;
        row = 0;

        for (Artist artist : filteredArtists) {
            VBox artistBox = createArtistBox(artist);
            artistGrid.add(artistBox, column, row);
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }

        ScrollPane artistScroll = new ScrollPane(artistGrid);
        artistScroll.setFitToWidth(true);
        artistsTab.setContent(artistScroll);

        searchTabPane.getTabs().addAll(albumsTab, artistsTab);
        // Create main layout that includes both content and player
        VBox rootLayout = new VBox(0);
        rootLayout.getStyleClass().add("main-layout");
        
        // Add main content
        VBox mainContent = new VBox(20);
        mainContent.getChildren().addAll(backButton, searchTabPane);
        mainContent.getStyleClass().add("content-area");
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Set common style for player controls
        sharedPlayerControls.getStyleClass().addAll("player-controls");
        sharedPlayerControls.setPadding(new Insets(10));
        sharedPlayerControls.setStyle("-fx-background-color: #282828;"); // Dark background like Spotify
        
        rootLayout.getChildren().addAll(mainContent, sharedPlayerControls);
        
        switchToView(rootLayout);
    }

    // Removed unused grid update methods as they're no longer needed
    // The grid content is now handled directly in createAlbumGrid() and
    // createArtistGrid()

    private void playSelectedSong(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // Fix the file path to point to the correct location in resources
            String resourcePath = "/music/" + song.getFilePath().substring(song.getFilePath().lastIndexOf('/') + 1);
            String uri = getClass().getResource(resourcePath).toURI().toString();
            System.out.println("Playing file from URI: " + uri);
            
            Media media = new Media(uri);
            mediaPlayer = new MediaPlayer(media);
            
            // Set volume
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            
            // Add listener for errors
            mediaPlayer.setOnError(() -> {
                System.err.println("Media player error: " + mediaPlayer.getError());
                Platform.runLater(() -> {
                    currentSongLabel.setText("Error playing: " + song.getTitle());
                    playButton.setDisable(true);
                    pauseButton.setDisable(true);
                });
            });
            
            // Update UI when media is ready
            mediaPlayer.setOnReady(() -> {
                Platform.runLater(() -> {
                    // Set song info
                    Artist artist = artistDAO.getArtistById(song.getArtistId());
                    String artistName = artist != null ? artist.getName() : "Unknown Artist";
                    currentSongLabel.setText(song.getTitle() + " - " + artistName);
                    
                    // Enable controls
                    playButton.setDisable(false);
                    pauseButton.setDisable(false);
                    
                    // Start playing
                    mediaPlayer.play();
                });
            });
            
            // Setup button actions
            playButton.setOnAction(e -> {
                mediaPlayer.play();
                playButton.setDisable(true);
                pauseButton.setDisable(false);
            });
            
            pauseButton.setOnAction(e -> {
                mediaPlayer.pause();
                playButton.setDisable(false);
                pauseButton.setDisable(true);
            });
            
            // Add listener for when song finishes
            mediaPlayer.setOnEndOfMedia(() -> {
                Platform.runLater(() -> {
                    currentSongLabel.setText("No song playing");
                    playButton.setDisable(true);
                    pauseButton.setDisable(true);
                });
            });
            
            // Initial state
            playButton.setDisable(true);
            pauseButton.setDisable(false);
            
        } catch (Exception e) {
            System.err.println("Error playing song: " + e.getMessage());
            currentSongLabel.setText("Error playing: " + song.getTitle());
            playButton.setDisable(true);
            pauseButton.setDisable(true);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            playButton.setDisable(false);
            pauseButton.setDisable(true);
        }
    }

    private void resumePlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            playButton.setDisable(true);
            pauseButton.setDisable(false);
        }
    }

    private void showPlaylistSongsDialog(Playlist playlist) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(playlist.getName());
        dialog.setHeaderText("Songs in Playlist");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().add("playlist-dialog");

        ListView<Song> songListView = new ListView<>();
        songListView.getItems().addAll(playlist.getSongs());
        songListView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                } else {
                    setText(song.getTitle());
                }
            }
        });

        songListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = songListView.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    playSelectedSong(selectedSong);
                    dialog.close();
                }
            }
        });

        dialogPane.setContent(songListView);
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }
}