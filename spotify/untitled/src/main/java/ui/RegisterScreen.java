package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.User;
import service.AuthService;

public class RegisterScreen {
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private final AuthService authService;
    private final Stage stage;

    public RegisterScreen(Stage stage) {
        this.stage = stage;
        this.authService = new AuthService();
    }

    public void show() {
        stage.setTitle("MySpotify - Register");

        // Create the main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setStyle("-fx-background-color: #282828;");

        // Create the form elements
        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(300);

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #1DB954;");
        registerButton.setOnAction(e -> handleRegister());

        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #535353;");
        backButton.setOnAction(e -> showLoginScreen());

        // Add elements to layout
        mainLayout.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameField,
            passwordField,
            confirmPasswordField,
            registerButton,
            backButton
        );

        // Create and show the scene
        Scene scene = new Scene(mainLayout, 400, 500);
        stage.setScene(scene);
        stage.show();
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Registration Error", "Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Registration Error", "Passwords do not match");
            return;
        }

        try {
            User user = authService.register(username, password);
            if (user != null) {
                showInfo("Registration Successful", "You can now login with your credentials");
                showLoginScreen();
            } else {
                showError("Registration Failed", "Username already exists");
            }
        } catch (Exception e) {
            showError("Error", "An error occurred during registration: " + e.getMessage());
        }
    }

    private void showLoginScreen() {
        new LoginScreen().start(stage);
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
}
