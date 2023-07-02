package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SampleEditPlaylistController {
    @FXML private TextField nameField;
    @FXML private ListView<String> songListView;

    private Playlist playlist;

    @FXML
    void initialize() {
        nameField.textProperty().addListener((observable, oldValue, newValue) -> playlist.name = newValue);
    }

    @FXML
    void onAddSongButtonPress() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio files (*.wav, *.mp3)", "*.wav", "*.mp3"));

            List<File> files = fileChooser.showOpenMultipleDialog(null);
            files.stream().forEachOrdered(f -> playlist.addSong(f.getAbsolutePath()));
            update();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load song").showAndWait();
        }
    }

    @FXML
    void onLoadFolderPress() {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);
        if (directory == null) return;
        Arrays.stream(directory.listFiles((dir, name) -> name.endsWith(".mp3") || name.endsWith(".wav"))).forEachOrdered(f -> playlist.addSong(f.getAbsolutePath()));
        update();
    }

    @FXML
    void onDownButtonPress() {
        int selectedIndex = songListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == playlist.getPlaylistLength() - 1) return;
        playlist.swapSongs(selectedIndex, selectedIndex + 1);
        update();
    }

    @FXML
    void onUpButtonPress() {
        int selectedIndex = songListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == 0) return;
        playlist.swapSongs(selectedIndex, selectedIndex - 1);
        update();
    }

    @FXML
    void onRemoveSongButtonPress() {
        if (songListView.getSelectionModel().getSelectedIndex() == -1) return;
        playlist.removeSong(songListView.getSelectionModel().getSelectedIndex());
        update();
    }

    void update() {
        nameField.textProperty().setValue(playlist.name);
        songListView.setItems(playlist.getSongNames());
    }

    Playlist getPlaylist() {
        return playlist;
    }

    void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        update();
    }
}
