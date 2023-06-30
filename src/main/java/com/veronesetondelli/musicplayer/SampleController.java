package com.veronesetondelli.musicplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class SampleController implements Runnable{
    @FXML
    private Button btn;
    ObservableList<Playlist> playlistList;
    int currentlyPlayingPlaylist;
    int currentlySelectedPlaylist;
    private boolean playing;
    private boolean skip;
    private boolean stop;
    private boolean jump;
    @FXML
    private Label currentPlaylistLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Label currentSongLabel;
    @FXML
    private ListView<String> songListView;
    @FXML
    private TableView<Playlist> playlistListTable;
    @FXML
    private TableColumn<Playlist, String> playlistNameColumn;

    @FXML
    void initialize() {
        currentlyPlayingPlaylist = 0;
        playing = false;
        skip = false;
        stop = true;
        jump = false;

        playlistList = FXCollections.observableArrayList();
        playlistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        playlistListTable.setItems(playlistList);

        playlistListTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            currentlySelectedPlaylist = newValue.intValue();
            songListView.getSelectionModel().clearSelection();
            songListView.setItems(playlistList.get(currentlySelectedPlaylist).getSongNames());
        });

        songListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("songListView " + oldValue.intValue() + " -> " + newValue.intValue());
            if (newValue.intValue() == -1) return;
            if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
                if (!stop) {
                    jump = true;
                    playlistList.get(currentlyPlayingPlaylist).player.stop();
                    playlistList.get(currentlyPlayingPlaylist).index = newValue.intValue();
                } else {
                    playlistList.get(currentlyPlayingPlaylist).index = newValue.intValue();
                    Thread t = new Thread(this);
                    t.start();
                }
            } else {
                jump = true;
                playlistList.get(currentlyPlayingPlaylist).player.stop();
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                playlistList.get(currentlyPlayingPlaylist).index = newValue.intValue();
            }
        });
    }

    @FXML
    void onPlayButtonClick() {
        if (stop) return;
        if (playing) {
            btn.setText("play");
            playlistList.get(currentlyPlayingPlaylist).pause();
            playing = false;
        }
        else {
            btn.setText("pause");
            playlistList.get(currentlyPlayingPlaylist).playCurrentIndex();
            playing = true;
        }
    }

    @FXML
    void onSkipButtonPress() {
        if (!stop) {
            skip = true;
            playlistList.get(currentlyPlayingPlaylist).player.stop();
        }
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
                playlistList.add(controller.getPlaylist());
                playlistListTable.setItems(playlistList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAddSongClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio files (*.wav, *.mp3)", "*.wav", "*.mp3"));

            File file = fileChooser.showOpenDialog(null);
            playlistList.get(currentlyPlayingPlaylist).addSong(file.getAbsolutePath());
            songListView.setItems(playlistList.get(currentlyPlayingPlaylist).getSongNames());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load song").showAndWait();
        }
    }

    @FXML
    void onVolumeButtonClick() {
        double currVol = playlistList.get(currentlyPlayingPlaylist).getVolume();
        if (currVol < 1.0) {
            playlistList.get(currentlyPlayingPlaylist).setVolume(currVol + 0.1);
        }
        else {
            playlistList.get(currentlyPlayingPlaylist).setVolume(0.0);
        }
    }

    @FXML
    void handlePlaylistFromFolder() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-playlist-from-folder-view.fxml"));
            DialogPane view = loader.load();
            SamplePlaylistFromFolderController controller = loader.getController();

            controller.setPlaylist(new Playlist("name"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Playlist");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                playlistList.add(controller.getPlaylist());
                playlistListTable.setItems(playlistList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLoadAsJSON() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

            File file = chooser.showOpenDialog(null);
            if (file != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<String> songs = mapper.readValue(file, new TypeReference<>() {});
                Playlist p = new Playlist(file.getName().substring(0, file.getName().length() - 5));
                p.addSongs(songs);
                playlistList.add(p);
                playlistListTable.setItems(playlistList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSaveAsJSON() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

            File file = chooser.showSaveDialog(null);
            if (file != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, playlistList.get(currentlySelectedPlaylist).songList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            playlistList.get(currentlyPlayingPlaylist).loadCurrentIndex();
            playlistList.get(currentlyPlayingPlaylist).playCurrentIndex();
            stop = false;
            playing = true;
            System.out.println("Playing " + playlistList.get(currentlyPlayingPlaylist).getCurrentSongName() + " from " + playlistList.get(currentlyPlayingPlaylist).name + " (" + currentlyPlayingPlaylist + ")");
            skip = false;
            Platform.runLater(() ->  {
                currentSongLabel.setText(playlistList.get(currentlyPlayingPlaylist).getCurrentSongName());
                songListView.getSelectionModel().clearSelection();
            });
            while (playlistList.get(currentlyPlayingPlaylist).player.isPlaying()) {
                if (stop) {
                    playing = false;
                    playlistList.get(currentlyPlayingPlaylist).player.stop();
                    return;
                }
                if (skip || jump) {
                    break;
                }
                Platform.runLater(() -> secondsLabel.setText(Double.toString(playlistList.get(currentlyPlayingPlaylist).player.getPlayingTimeSeconds())));
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!jump) {
                playlistList.get(currentlyPlayingPlaylist).nextSong();
            } else {
                jump = false;
            }
        }
    }
}