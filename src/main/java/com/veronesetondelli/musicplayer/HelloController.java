package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {
    @FXML
    private Label welcomeText;
    AudioPlayer audioPlayer = new AudioPlayer();
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
        if (!audioPlayer.isPlaying) {
            audioPlayer.loadAudioFile("C:\\Users\\samue\\IdeaProjects\\testClip\\src\\main\\resources\\chepalle.wav");
            Thread t = new Thread(audioPlayer);
            t.start();
        }
    }
}