package com.empresa.hito_programacion_daniel_jimenez;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if ("Daniel".equals(username) && "123".equals(password)) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(fxmlLoader.load(), 600, 480));
            } catch (IOException e) {
                errorLabel.setText("Error al cargar la siguiente vista.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Usuario o contraseña incorrectos.");
        }
    }
}