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

        // Left: Image pane
        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("login-image-pane");

        // Right: Register form
        VBox mainLayout = new VBox();
        mainLayout.getStyleClass().add("login-container");

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("login-title");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.getStyleClass().add("login-field");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-field");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.getStyleClass().add("login-field");

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("login-button");
        registerButton.setOnAction(e -> handleRegister());

        Button backButton = new Button("Back to Login");
        backButton.getStyleClass().add("register-button");
        backButton.setOnAction(e -> showLoginScreen());

        mainLayout.getChildren().addAll(
            titleLabel,
            new Separator(),
            usernameField,
            passwordField,
            confirmPasswordField,
            registerButton,
            new Separator(),
            backButton
        );

        // Split layout
        HBox root = new HBox();
        root.getStyleClass().add("login-root");
        root.getChildren().addAll(imagePane, mainLayout);
        HBox.setHgrow(imagePane, Priority.ALWAYS);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        Scene scene = new Scene(root, 800, 600);
        String cssPath = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
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
