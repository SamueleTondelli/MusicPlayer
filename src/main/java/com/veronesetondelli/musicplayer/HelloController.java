package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private Button btn;
    Playlist playlist;
    @FXML
    protected void onLoadButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        if (playlist == null) {
            playlist = new Playlist();
            playlist.addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\chepalle.wav");
            playlist.addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\vecchio.wav");
            Thread t = new Thread(playlist);
            t.start();
        }
        else if (playlist.stop) {
            playlist.index = 0;
            Thread t = new Thread(playlist);
            t.start();
        }
    }

    @FXML
    void onPlayButtonClick() {
        if (playlist.stop) return;
        if (playlist.playing) {
            btn.setText("play");
            playlist.pause();
        }
        else {
            btn.setText("pause");
            playlist.playCurrentIndex();
        }
    }

    @FXML
    void onSkipButtonPress() {
        if (!playlist.stop) {playlist.skip = true;}
    }

    @FXML
    void onStopButtonPress() {
        btn.setText("pause");
        playlist.stop = true;
    }
}