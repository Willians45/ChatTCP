package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.InetAddress;

public class IntroController {

    @FXML
    private TextField serverAddressField;
    @FXML
    private TextField serverPortField;
    @FXML
    private TextField userNameField;
    @FXML
    private Button connectButton;
    @FXML
    private ImageView logo;

    private MainClient mainClient;

    @FXML
    private void initialize() {

        serverAddressField.setPromptText("server address");
        serverAddressField.setText("localhost");
        serverPortField.setPromptText("server port");
        serverPortField.setText("7777");
        userNameField.setPromptText("user name");

        connectButton.setDefaultButton(true);

        try {
            System.out.printf("\nclient running(%s)...\n\n", InetAddress.getLocalHost());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("-->Imposible encontrar localhost.");
            System.out.println("------------------------------------------------->");
        }
    }

    public void setMainClient(MainClient mainClient) {
        this.mainClient = mainClient;
    }

    @FXML
    private void handleConnectButton() {

        if (serverPortField.getText().isEmpty() || serverAddressField.getText().isEmpty()
                || userNameField.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainClient.getStage());
            alert.setTitle("Error");
            alert.setHeaderText("Campo Vacío");
            alert.setContentText("Por favor, ingrese datos válidos");
            alert.showAndWait();

        } else {
            String serverAddress = serverAddressField.getText();
            int serverPort = Integer.parseInt(serverPortField.getText());
            String userName = userNameField.getText();

            boolean connectedSuccessful = TCPClient.connectToServer(serverAddress, serverPort, userName, this);

            if (connectedSuccessful)
                mainClient.initChatScene();
        }
    }

    public void showWarningAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(mainClient.getStage());
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
