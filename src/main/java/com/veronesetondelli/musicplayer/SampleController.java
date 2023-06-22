package com.veronesetondelli.musicplayer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class SampleController {
    @FXML
    private Button btn;
    ArrayList<Playlist> playlist = new ArrayList<>();
    int currentlySelectedPlaylist = 0;
    @FXML
    private Label currentPlaylistLabel;
    @FXML
    protected void onLoadButtonClick() {
        if (playlist.size() == 0) {
            Playlist p = new Playlist("p1");
            p.addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\chepalle.wav");
            p.addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\vecchio.wav");
            playlist.add(p);
            currentPlaylistLabel.setText(p.name);
            Thread t = new Thread(playlist.get(0));
            t.start();
        }
        else if (playlist.get(currentlySelectedPlaylist).stop) {
            playlist.get(currentlySelectedPlaylist).index = 0;
            Thread t = new Thread(playlist.get(currentlySelectedPlaylist));
            t.start();
        }
    }

    @FXML
    void onPlayButtonClick() {
        if (playlist.get(currentlySelectedPlaylist).stop) return;
        if (playlist.get(currentlySelectedPlaylist).playing) {
            btn.setText("play");
            playlist.get(currentlySelectedPlaylist).pause();
        }
        else {
            btn.setText("pause");
            playlist.get(currentlySelectedPlaylist).playCurrentIndex();
        }
    }

    @FXML
    void onSkipButtonPress() {
        if (!playlist.get(currentlySelectedPlaylist).stop) {playlist.get(currentlySelectedPlaylist).skip = true;}
    }

    @FXML
    void onStopButtonPress() {
        btn.setText("pause");
        playlist.get(currentlySelectedPlaylist).stop = true;
    }

    @FXML
    void handleCreatePlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-new-playlist-view.fxml"));
            DialogPane view = loader.load();
            SampleNewPlaylistController controller = loader.getController();

            controller.setPlaylist(new Playlist("name"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Playlist");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                playlist.add(controller.getPlaylist());
                playlist.get(playlist.size() - 1).addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main" +
                        "\\resources\\com\\veronesetondelli" + "\\musicplayer\\vecchio.wav");
                playlist.get(playlist.size() - 1).addSong("C:\\Users\\samue\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\chepalle.wav");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onNextPlaylistButtonClick() {
        currentlySelectedPlaylist = (currentlySelectedPlaylist + 1) % playlist.size();
        currentPlaylistLabel.setText(playlist.get(currentlySelectedPlaylist).name);
    }

    @FXML
    void handleAddSongClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV files (*.wav)", "*.wav"));

            File file = fileChooser.showOpenDialog(null);
            playlist.get(currentlySelectedPlaylist).addSong(file.getAbsolutePath());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load song").showAndWait();
        }
    }
}