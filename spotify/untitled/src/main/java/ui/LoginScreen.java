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

public class LoginScreen extends Application {
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;
    private AuthService authService;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MySpotify - Login");

        // Initialize auth service
        authService = new AuthService();

        // Create the main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #121212;");

        // Create the form
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        // Username field
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-text-fill: white;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);

        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-text-fill: white;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);

        // Message label for feedback
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #1DB954;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        buttonBox.getChildren().addAll(loginButton, registerButton);

        // Add all components to main layout
        mainLayout.getChildren().addAll(form, messageLabel, buttonBox);

        // Add event handlers
        loginButton.setOnAction(e -> handleLogin(primaryStage));
        registerButton.setOnAction(e -> handleRegister());

        // Create the scene
        Scene scene = new Scene(mainLayout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin(Stage primaryStage) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }

        try {
            User user = authService.login(username, password);
            if (user != null) {
                messageLabel.setText("Login successful!");
                // Open the main player window
                MainPlayer mainPlayer = new MainPlayer(user);
                Stage playerStage = new Stage();
                mainPlayer.start(playerStage);
                // Close the login window
                primaryStage.close();
            } else {
                messageLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }

        try {
            User newUser = authService.register(username, password);
            if (newUser != null) {
                messageLabel.setText("Registration successful! You can now login.");
                // Clear the fields
                usernameField.clear();
                passwordField.clear();
            }
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 