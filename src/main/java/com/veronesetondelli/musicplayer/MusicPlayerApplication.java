package com.veronesetondelli.musicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class for the application
 */
public class MusicPlayerApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MusicPlayerApplication.class.getResource("music-player-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 600);
        MusicPlayerController controller = fxmlLoader.getController();
        // Callback when the window closes
        stage.setOnCloseRequest((e) -> controller.handleClose());
        stage.setTitle("Music Player");
        stage.setScene(scene);
        stage.setMinHeight(400);
        stage.setMinWidth(600);
        stage.show();
    }
}