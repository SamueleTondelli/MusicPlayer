package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SampleNewPlaylistController {

    @FXML
    private TextField playlistNameField;
    Playlist playlist;

    @FXML
    public void initialize() {
        playlistNameField.textProperty().addListener((observable, oldValue, newValue) -> playlist.name = newValue);
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