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
        currentlyPlayingPlaylist = -1;
        playing = false;
        skip = false;
        stop = true;
        jump = false;

        playlistList = FXCollections.observableArrayList();
        playlistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        playlistListTable.setItems(playlistList);

        playlistListTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("playlistListTable " + oldValue.intValue() + " -> " + newValue.intValue());
            currentlySelectedPlaylist = newValue.intValue();
            songListView.getSelectionModel().clearSelection();
            if (currentlySelectedPlaylist != -1) {
                songListView.setItems(getSelectedPlaylist().getSongNames());
            }
        });

        songListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("songListView " + oldValue.intValue() + " -> " + newValue.intValue());
            if (newValue.intValue() == -1) return;
            if (currentlyPlayingPlaylist == -1) {
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getSelectedPlaylist().index = newValue.intValue();
                Thread t = new Thread(this);
                t.start();
            }  else if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
                if (!stop) {
                    jump = true;
                    getSelectedPlaylist().player.stop();
                    getSelectedPlaylist().index = newValue.intValue();
                } else {
                    getSelectedPlaylist().index = newValue.intValue();
                    Thread t = new Thread(this);
                    t.start();
                }
            } else {
                jump = true;
                getPlayingPlaylist().player.stop();
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getPlayingPlaylist().index = newValue.intValue();
            }
        });
    }

    @FXML
    void onPlayButtonClick() {
        if (stop) return;
        if (playing) {
            btn.setText("play");
            getPlayingPlaylist().pause();
            playing = false;
        }
        else {
            btn.setText("pause");
            getPlayingPlaylist().playCurrentIndex();
            playing = true;
        }
    }

    @FXML
    void onSkipButtonPress() {
        if (!stop) {
            skip = true;
            getPlayingPlaylist().player.stop();
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
            getPlayingPlaylist().addSong(file.getAbsolutePath());
            songListView.setItems(getPlayingPlaylist().getSongNames());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load song").showAndWait();
        }
    }

    @FXML
    void onVolumeButtonClick() {
        double currVol = getPlayingPlaylist().getVolume();
        if (currVol < 1.0) {
            getPlayingPlaylist().setVolume(currVol + 0.1);
        }
        else {
            getPlayingPlaylist().setVolume(0.0);
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
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getSelectedPlaylist().songList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onEditPlaylistButtonPress() {
        if (playlistListTable.getSelectionModel().getSelectedIndex() == -1) return;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-edit-view.fxml"));
            DialogPane view = loader.load();
            SampleEditPlaylistController controller = loader.getController();

            int editingPlaylistIndex = currentlySelectedPlaylist;
            if (editingPlaylistIndex == currentlyPlayingPlaylist) stop = true;
            controller.setPlaylist(playlistList.get(editingPlaylistIndex));
            controller.update();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Playlist");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                playlistList.set(editingPlaylistIndex, controller.getPlaylist());
                playlistListTable.setItems(playlistList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onRemovePlaylistButtonPress() {
        if (playlistList.size() == 0 || currentlySelectedPlaylist == -1) return;
        if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
            stop = true;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        playlistList.remove(currentlySelectedPlaylist);
        playlistListTable.setItems(playlistList);
        playlistListTable.getSelectionModel().clearSelection();
        songListView.setItems(null);
    }

    @Override
    public void run() {
        while (true) {
            getPlayingPlaylist().loadCurrentIndex();
            getPlayingPlaylist().playCurrentIndex();
            stop = false;
            playing = true;
            System.out.println("Playing " + getPlayingPlaylist().getCurrentSongName() + " from " + getPlayingPlaylist().name + " (" + currentlyPlayingPlaylist + ")");
            skip = false;
            jump = false;
            Platform.runLater(() ->  {
                currentSongLabel.setText(getPlayingPlaylist().getCurrentSongName());
                songListView.getSelectionModel().clearSelection();
            });
            while (getPlayingPlaylist().player.isPlaying()) {
                if (stop) {
                    playing = false;
                    getPlayingPlaylist().player.stop();
                    return;
                }
                if (skip || jump) {
                    break;
                }
                Platform.runLater(() -> secondsLabel.setText(Double.toString(getPlayingPlaylist().player.getPlayingTimeSeconds())));
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (getPlayingPlaylist().getPlaylistLength() == 0) return;
            if (!jump) {
                getPlayingPlaylist().nextSong();
            }
        }
    }
    
    Playlist getSelectedPlaylist() { return playlistList.get(currentlySelectedPlaylist); }
    Playlist getPlayingPlaylist() { return playlistList.get(currentlyPlayingPlaylist); }
}