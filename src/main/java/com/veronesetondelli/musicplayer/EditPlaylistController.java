/*
 * The MIT License
 *
 * Copyright 2023 SamueleTondelli, alexveronese
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Controller class for EditPlaylist
 */
public class EditPlaylistController {
    @FXML
    private TextField nameField;
    @FXML
    private TableView<Song> songTableView;
    @FXML
    private TableColumn<Song, String> locationColumn;
    @FXML
    private TableColumn<Song, String> nameColumn;

    private Playlist playlist;

    /**
     * Initializes column of the songTableView and the nameField with its listener
     */
    @FXML
    void initialize() {
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        nameField.textProperty().addListener((observable, oldValue, newValue) -> playlist.setName(newValue));
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

    /**
     * Updates nameField with playlist name and the songTableView with the songs in the playlist
     */
    void update() {
        nameField.textProperty().setValue(playlist.getName());
        songTableView.setItems(playlist.getSongList());
    }

    @FXML
    void onLoadFolderPress() {
        DirectoryChooser chooser = new DirectoryChooser();
        File directory = chooser.showDialog(null);
        if (directory == null)
            return;
        Arrays.stream(directory.listFiles((dir, name) -> name.endsWith(".mp3") || name.endsWith(".wav"))).forEachOrdered(f -> playlist.addSong(f.getAbsolutePath()));
        update();
    }

    @FXML
    void onDownButtonPress() {
        int selectedIndex = songTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == playlist.getPlaylistLength() - 1)
            return;
        playlist.swapSongs(selectedIndex, selectedIndex + 1);
        update();
    }

    @FXML
    void onUpButtonPress() {
        int selectedIndex = songTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == 0)
            return;
        playlist.swapSongs(selectedIndex, selectedIndex - 1);
        update();
    }

    @FXML
    void onRemoveSongButtonPress() {
        if (songTableView.getSelectionModel().getSelectedIndex() == -1)
            return;
        playlist.removeSong(songTableView.getSelectionModel().getSelectedIndex());
        update();
    }

    Playlist getPlaylist() {
        return playlist;
    }

    void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
        update();
    }
}
