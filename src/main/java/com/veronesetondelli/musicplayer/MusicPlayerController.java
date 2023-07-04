package com.veronesetondelli.musicplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

public class MusicPlayerController implements Runnable{
    @FXML
    private Button btnPlay;

    @FXML
    private Button muteBtn;

    @FXML
    private Label volumeLabel;

    ObservableList<Playlist> playlistList;
    private int currentlyPlayingPlaylist;
    private int currentlySelectedPlaylist;
    private boolean playing;
    private boolean skip;
    private boolean previous;
    private boolean stop;
    private boolean mute;
    private boolean updateProgressBar;
    private boolean jump;
    private double currVol;
    private Thread t;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label currentPlaylistLabel;
    @FXML
    private Label currentSongProgress;
    @FXML
    private Label currentSongLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Slider songProgressSlider;
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
        previous = false;
        stop = true;
        mute = false;
        updateProgressBar = true;
        jump = false;

        playlistList = FXCollections.observableArrayList();
        playlistNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        playlistListTable.setItems(playlistList);

        playlistListTable.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            currentlySelectedPlaylist = newValue.intValue();
            songListView.getSelectionModel().clearSelection();
            if (currentlySelectedPlaylist != -1) {
                songListView.setItems(getSelectedPlaylist().getSongNames());
            }
        });

        songListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == -1) return;
            if (currentlyPlayingPlaylist == -1) {
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getSelectedPlaylist().index = newValue.intValue();
                t = new Thread(this);
                t.start();
            }  else if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
                if (!stop) {
                    jump = true;
                    getSelectedPlaylist().player.stop();
                    getSelectedPlaylist().index = newValue.intValue();
                } else {
                    getSelectedPlaylist().index = newValue.intValue();
                    t = new Thread(this);
                    t.start();
                }
            } else {
                jump = true;
                getPlayingPlaylist().player.stop();
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getPlayingPlaylist().index = newValue.intValue();
            }
        });

        volumeSlider.setValue(volumeSlider.getMax());

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                getPlayingPlaylist().setVolume(volumeSlider.getValue() * 0.01);
            }
        });
    }
    @FXML
    protected void onLoadButtonClick() {
        if (playlistList.size() == 0) {
            Playlist p = new Playlist("p1");
            p.addSong("C:\\Users\\samue\\Desktop\\songs\\chepalle.wav");
            p.addSong("C:\\Users\\samue\\Desktop\\songs\\vecchio.wav");
            playlistList.add(p);
            //currentlyPlayingPlaylist = 0;
            //currentPlaylistLabel.setText(p.name);
            //t = new Thread(this);

            //t.start();
        }
    }
    @FXML
    void onPlayButtonClick() {
        if (stop) return;
        if (playing) {
            getPlayingPlaylist().pause();
            playing = false;
        }
        else {
            //btn.setText("pause");
            getPlayingPlaylist().playCurrentIndex();
            playing = true;
        }
    }
    @FXML
    void onSkipButtonPress() {
        if (!stop) {skip = true;}
    }
    @FXML
    void onPreviousButtonPress() {
        if (!stop) {
            previous = true;
            getPlayingPlaylist().previousSong();
        }
    }
    @FXML
    void onStopButtonPress() {
        //btn.setText("pause");
        stop = true;
    }
    @FXML
    void handleCreatePlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-new-playlist-view.fxml")); //!!!!!!!
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handlePlaylistFromFolder() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-playlist-from-folder-view.fxml")); //!!!!!!
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void onNextPlaylistButtonClick() {
        currentlyPlayingPlaylist = (currentlyPlayingPlaylist + 1) % playlistList.size();
        currentPlaylistLabel.setText(getPlayingPlaylist().name);
        songListView.setItems(getPlayingPlaylist().getSongNames());
    }
    @FXML
    void handleAddSongClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio files (*.wav, *.mp3)", "*.wav", "*.mp3"));

            File file = fileChooser.showOpenDialog(null);
            getSelectedPlaylist().addSong(file.getAbsolutePath());
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Could not load song").showAndWait();
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
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getPlayingPlaylist().songList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void onMuteButtonClick() {
        if(!mute) {
            mute = true;
            muteBtn.setText("Unmute");
            currVol = volumeSlider.getValue();
            volumeSlider.setValue(0.0);
        } else {
            mute = false;
            muteBtn.setText("Mute");
            volumeSlider.setValue(currVol);
        }
    }
    @FXML
    void handleSongProgressChange() {
        getPlayingPlaylist().setCurrentSongProgress(songProgressSlider.getValue());
        updateProgressBar = true;
    }
    @FXML
    void onSongProgressSliderPress() {
        updateProgressBar = false;
    }
    Playlist getPlayingPlaylist() { return playlistList.get(currentlyPlayingPlaylist); }
    Playlist getSelectedPlaylist() { return playlistList.get(currentlySelectedPlaylist); }
    @Override
    public void run() {
        while (true) {
            getPlayingPlaylist().loadCurrentIndex();
            getPlayingPlaylist().playCurrentIndex();
            stop = false;
            playing = true;
            skip = false;
            previous = false;
            jump = false;
            getPlayingPlaylist().setVolume(volumeSlider.getValue() * 0.01);

            Platform.runLater(() -> {
                currentSongLabel.setText(getPlayingPlaylist().getCurrentSongName());
                songListView.getSelectionModel().clearSelection();
            });
            while (getPlayingPlaylist().player.isPlaying()) {
                if (updateProgressBar) {
                    Platform.runLater(() -> {
                            songProgressSlider.setValue(getPlayingPlaylist().getCurrentSongProgress());
                            secondsLabel.setText(Double.toString(getPlayingPlaylist().player.getPlayingTimeSeconds()));
                            volumeLabel.setText(Long.toString(Math.round(volumeSlider.getValue())));
                            //currentSongProgress.setText(Double.toString(getPlayingPlaylist().getCurrentSongProgress()));
                        });
                }
                if (stop) {
                    playing = false;
                    getPlayingPlaylist().player.stop();
                    return;
                }
                if (skip || jump || previous) {
                    getPlayingPlaylist().player.stop();
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            System.out.println("Exited loop with stop " + stop + ", playing " + playing + ", skip " + skip + ", " +
                    "previous " + previous + ", jump " + jump);
            if (getPlayingPlaylist().getPlaylistLength() == 0) return;
            if (!jump) {
                getPlayingPlaylist().nextSong();
            }
        }
    }
}