package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;
import java.util.Scanner;

public class ClientController {

    @FXML
    private TextField userInputField;
    @FXML
    private TextArea chatField;
    @FXML
    private TextArea activeUsersField;
    @FXML
    private Button sendButton;

    private MainClient mainClient;

    @FXML
    private void initialize() {

        chatField.setText("Bienvenido!\nPor favor escribe algo y luego presiona 'Send'\n\n");
        userInputField.setPromptText("Escriba algo pa'Send'");
        sendButton.setDefaultButton(true);
    }

    public void setMainClient(MainClient mainClient) {
        this.mainClient = mainClient;
    }

    @FXML
    private void handleSendButton() {
        if (userInputField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainClient.getStage());
            alert.setTitle("Error");
            alert.setHeaderText("No hay mensajes");
            alert.setContentText("Please enter your message before you hit \"Send\"");
            alert.showAndWait();

        } else {
            TCPClient.sendButton(userInputField.getText());
            userInputField.clear();
        }
    }

    public void handleChatField(String message) {
        chatField.appendText(message + "\n");
    }

    public void handleActiveUsersField(String message) {
        activeUsersField.setText("Usuarios Activos:\n");

        Scanner scanner = new Scanner(message);
        while (scanner.hasNext()) {
            activeUsersField.appendText(scanner.next() + "\n");
        }
        scanner.close();
    }

    public void setExit() {
        mainClient.getStage().setOnCloseRequest(e -> {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(mainClient.getStage());
            alert.setTitle("Confirmación");
            alert.setHeaderText(null);
            alert.setContentText("Estás seguro?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK) {
                TCPClient.exit();

            } else {
                e.consume();
                alert.close();
            }
        });
    }

}
