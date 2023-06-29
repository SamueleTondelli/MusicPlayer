package com.veronesetondelli.musicplayer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class SamplePlaylistFromFolderController {

    @FXML
    private TextField playlistNameField;
    Playlist playlist;

    @FXML
    public void initialize() {
        playlistNameField.textProperty().addListener((observable, oldValue, newValue) -> playlist.name = newValue);
    }
    @FXML
    void onChooseFolderButtonPress(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);
        playlist.removeAllSongs();
        for (File f : directory.listFiles()) {
            if (f.getAbsolutePath().endsWith("mp3") || f.getAbsolutePath().endsWith("wav")) {
                playlist.addSong(f.getAbsolutePath());
            }
        }
    }

    void update() {
        playlistNameField.textProperty().set(playlist.name);
    }

    public Playlist getPlaylist() {
        return playlist;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }
}