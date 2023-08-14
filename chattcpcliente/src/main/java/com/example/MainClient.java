package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainClient extends Application {

    private Stage stage;
    private Pane layout;

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        initIntroScene();
    }

    public void initChatScene() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainClient.class.getResource("client.fxml"));
            layout = loader.load();

            ClientController clientController = loader.getController();
            clientController.setMainClient(this);
            clientController.setExit();

            Scene scene = new Scene(layout, 655, 375);
            stage.setScene(scene);

            stage.setTitle("Subwoffersagge");

            stage.centerOnScreen();

            Thread thread = new Thread() {
                @Override
                public void run() {
                    TCPClient.initialize(clientController);
                }
            };
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initIntroScene() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainClient.class.getResource("intro.fxml"));
            layout = loader.load();

            IntroController introController = loader.getController();
            introController.setMainClient(this);

            Scene scene = new Scene(layout, 400, 350);
            stage.setScene(scene);
            stage.setTitle("Login");

            stage.show();
            stage.centerOnScreen();
            stage.setResizable(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
