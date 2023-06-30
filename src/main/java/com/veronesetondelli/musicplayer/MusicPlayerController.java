package com.veronesetondelli.musicplayer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicPlayerController implements Runnable{
    @FXML
    private Button btnPlay;

    @FXML
    private Button muteBtn;

    @FXML
    private Label volumeLabel;

    ArrayList<Playlist> playlist = new ArrayList<>();
    int currentlySelectedPlaylist = 0;
    private boolean playing = false;
    private boolean skip = false;
    private boolean previous = false;
    private boolean stop = true;
    private boolean mute = false;
    private boolean updateProgressBar = true;
    private boolean jump = false;
    private double currVol;
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
    void initialize() {
        songListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            playlist.get(currentlySelectedPlaylist).index = newValue.intValue();
            jump = true;
        });

        volumeSlider.setValue(volumeSlider.getMax());

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                playlist.get(currentlySelectedPlaylist).setVolume(volumeSlider.getValue() * 0.01);
            }
        });
    }
    @FXML
    protected void onLoadButtonClick() {
        if (playlist.size() == 0) {
            Playlist p = new Playlist("p1");
            p.addSong("C:\\Users\\veron\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\chepalle.wav");
            p.addSong("C:\\Users\\veron\\IdeaProjects\\MusicPlayer\\src\\main\\resources\\com\\veronesetondelli" + "\\musicplayer\\vecchio.wav");
            playlist.add(p);
            //currentPlaylistLabel.setText(p.name);
            Thread t = new Thread(this);
            t.start();
        }
        else if (stop) {
            playlist.get(currentlySelectedPlaylist).index = 0;
            Thread t = new Thread(this);
            t.start();
        }
    }
    @FXML
    void onPlayButtonClick() {
        if (stop) return;
        if (playing) {
            playlist.get(currentlySelectedPlaylist).pause();
            playing = false;
        }
        else {
            //btn.setText("pause");
            playlist.get(currentlySelectedPlaylist).playCurrentIndex();
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
            playlist.get(currentlySelectedPlaylist).previousSong();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                playlist.add(controller.getPlaylist());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void onNextPlaylistButtonClick() {
        currentlySelectedPlaylist = (currentlySelectedPlaylist + 1) % playlist.size();
        currentPlaylistLabel.setText(playlist.get(currentlySelectedPlaylist).name);
        songListView.setItems(playlist.get(currentlySelectedPlaylist).getSongNames());
    }
    @FXML
    void handleAddSongClick() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio files (*.wav, *.mp3)", "*.wav", "*.mp3"));

            File file = fileChooser.showOpenDialog(null);
            playlist.get(currentlySelectedPlaylist).addSong(file.getAbsolutePath());
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
                playlist.add(p);
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
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, playlist.get(currentlySelectedPlaylist).songList);
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
        playlist.get(currentlySelectedPlaylist).setCurrentSongProgress(songProgressSlider.getValue());
        updateProgressBar = true;
    }
    @FXML
    void onSongProgressSliderPress() {
        updateProgressBar = false;
    }
    @Override
    public void run() {
        playlist.get(currentlySelectedPlaylist).loadCurrentIndex();
        playlist.get(currentlySelectedPlaylist).playCurrentIndex();
        stop = false;
        playing = true;
        while (true) {
            playlist.get(currentlySelectedPlaylist).setVolume(volumeSlider.getValue() * 0.01);
            skip = false;
            previous = false;

            Platform.runLater(new Runnable() {
                public void run() {
                    currentSongLabel.setText(playlist.get(currentlySelectedPlaylist).getCurrentSongName());
                }
            });

            while (playlist.get(currentlySelectedPlaylist).player.isPlaying()) {
                if (updateProgressBar) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            songProgressSlider.setValue(playlist.get(currentlySelectedPlaylist).getCurrentSongProgress());
                            secondsLabel.setText(Double.toString(playlist.get(currentlySelectedPlaylist).player.getPlayingTimeSeconds()));
                            volumeLabel.setText(Long.toString(Math.round(volumeSlider.getValue())));
                            currentSongProgress.setText(Double.toString(playlist.get(currentlySelectedPlaylist).getCurrentSongProgress()));
                        }
                    });
                }
                if (stop) {
                    playing = false;
                    playlist.get(currentlySelectedPlaylist).player.stop();
                    return;
                }
                if (skip || jump || previous) {
                    playlist.get(currentlySelectedPlaylist).player.stop();
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (!jump) {
                playlist.get(currentlySelectedPlaylist).nextSong();
            } else {
                jump = false;
            }


            playlist.get(currentlySelectedPlaylist).loadCurrentIndex();
            playlist.get(currentlySelectedPlaylist).playCurrentIndex();
        }
    }
}