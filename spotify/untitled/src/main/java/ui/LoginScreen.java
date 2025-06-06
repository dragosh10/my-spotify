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

        // Create the main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #282828;");

        // Create the form elements
        Label titleLabel = new Label("Welcome to MySpotify");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #1DB954; -fx-text-fill: white;");
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> handleLogin());

        Button registerButton = new Button("Create Account");
        registerButton.setStyle("-fx-background-color: #535353; -fx-text-fill: white;");
        registerButton.setMaxWidth(300);
        registerButton.setOnAction(e -> showRegisterScreen(primaryStage));

        // Add elements to layout
        mainLayout.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameField,
            passwordField,
            loginButton,
            new Separator(),
            registerButton
        );

        // Create and show the scene
        Scene scene = new Scene(mainLayout, 400, 500);
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

