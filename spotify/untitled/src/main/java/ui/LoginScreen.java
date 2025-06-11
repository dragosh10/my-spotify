package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.User;
import service.AuthService;
//cd /home/kaboodlecat/Documents/my-spotify/spotify/untitled && mvn clean javafx:run
public class LoginScreen extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private final AuthService authService;

    public LoginScreen() {
        this.authService = new AuthService();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MySpotify - Login");

        // Left: Image
        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("login-image-pane");
        // Example: set a background image via CSS, or use ImageView if you want
        // ImageView imageView = new ImageView(new Image(getClass().getResource("/your-image.png").toExternalForm()));
        // imageView.setFitWidth(400); imageView.setPreserveRatio(true);
        // imagePane.getChildren().add(imageView);

        // Right: Login form
        VBox mainLayout = new VBox();
        mainLayout.getStyleClass().add("login-container");

        Label titleLabel = new Label("Welcome back!");
        titleLabel.getStyleClass().add("login-title");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("login-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-field");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("login-button");
        loginButton.setOnAction(e -> handleLogin());

        Button registerButton = new Button("Create Account");
        registerButton.getStyleClass().add("register-button");
        registerButton.setOnAction(e -> showRegisterScreen(primaryStage));

        mainLayout.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameField,
            passwordField,
            loginButton,
            new Separator(),
            registerButton
        );

        // Split layout
        HBox root = new HBox();
        root.getStyleClass().add("login-root");
        root.getChildren().addAll(imagePane, mainLayout);
        HBox.setHgrow(imagePane, Priority.ALWAYS);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // Scene
        Scene scene = new Scene(root, 800, 600);
        String cssPath = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = authService.login(username, password);
            if (user != null) {
                // Login successful, start the main player
                MainPlayer mainPlayer = new MainPlayer(user);
                Stage stage = (Stage) usernameField.getScene().getWindow();
                mainPlayer.start(stage);
            } else {
                showError("Login Failed", "Invalid username or password");
            }
        } catch (Exception e) {
            showError("Error", "An error occurred during login: " + e.getMessage());
        }
    }

    private void showRegisterScreen(Stage stage) {
        new RegisterScreen(stage).show();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

