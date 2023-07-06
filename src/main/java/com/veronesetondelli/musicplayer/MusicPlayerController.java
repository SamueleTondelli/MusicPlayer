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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class MusicPlayerController implements Runnable{

    @FXML
    private Button muteBtn;
    @FXML
    private Button playBtn;
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
    private Thread metadataLoaderThread;
    private Image imagePause = new Image(getClass().getResourceAsStream("pause-ret.png"));
    private Image imagePlay = new Image(getClass().getResourceAsStream("play-button-ret.png"));
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label currentSongLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Slider songProgressSlider;
    @FXML
    private TableView<Playlist> playlistListTable;
    @FXML
    private TableColumn<Playlist, String> playlistNameColumn;
    @FXML
    private TableView<Song> songTableView;
    @FXML
    private TableColumn<Song, String> titleColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;
    @FXML
    private TableColumn<Song, String> lengthColumn;

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
            songTableView.getSelectionModel().clearSelection();
            if (currentlySelectedPlaylist != -1) {
                songTableView.setItems(getSelectedPlaylist().getSongList());
            }
        });

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("durationFormatted"));
        songTableView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() == -1) return;
            if (currentlyPlayingPlaylist == -1) {
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getSelectedPlaylist().setIndex(newValue.intValue());
                t = new Thread(this);
                t.start();
            }  else if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
                if (!stop) {
                    jump = true;
                    getSelectedPlaylist().stopPlayer();
                    getSelectedPlaylist().setIndex(newValue.intValue());
                } else {
                    getSelectedPlaylist().setIndex(newValue.intValue());
                    t = new Thread(this);
                    t.start();
                }
            } else {
                jump = true;
                getPlayingPlaylist().stopPlayer();
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getPlayingPlaylist().setIndex(newValue.intValue());
            }
        });

        volumeSlider.setValue(volumeSlider.getMax());
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> getPlayingPlaylist().setVolume(newValue.doubleValue() * 0.01));
    }
    @FXML
    protected void onLoadButtonClick() {
        if (playlistList.size() == 0) {
            Playlist p = new Playlist("p1");
            p.addSong("C:\\Users\\veron\\Desktop\\playlist\\chepalle.wav");
            p.addSong("C:\\Users\\veron\\Desktop\\playlist\\vecchio.wav");
            playlistList.add(p);
            loadPlaylistMetadata(p);
        }
    }
    @FXML
    void onPlayButtonClick() {
        if (stop) return;
        if (playing) {
            getPlayingPlaylist().pause();
            playBtn.setGraphic(new ImageView(imagePause));
            playing = false;
        }
        else {
            getPlayingPlaylist().playCurrentIndex();
            playBtn.setGraphic(new ImageView(imagePlay));
            playing = true;
        }
    }
    @FXML
    void onSkipButtonPress() {
        if (!stop) {
            skip = true;
            playBtn.setGraphic(new ImageView(imagePlay));
        }
    }
    @FXML
    void onPreviousButtonPress() {
        if (!stop) {
            previous = true;
            playBtn.setGraphic(new ImageView(imagePlay));
            getPlayingPlaylist().previousSong();
        }
    }
    @FXML
    void handleCreatePlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("sample-edit-view.fxml"));
            DialogPane view = loader.load();
            SampleEditPlaylistController controller = loader.getController();

            controller.setPlaylist(new Playlist("name"));

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("New Playlist");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);

            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                Playlist p = controller.getPlaylist();
                playlistList.add(p);
                playlistListTable.setItems(playlistList);
                loadPlaylistMetadata(p);
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
                loadPlaylistMetadata(p);
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
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, getSelectedPlaylist().getSongsPathList());
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
            controller.setPlaylist(new Playlist(playlistList.get(editingPlaylistIndex)));
            controller.update();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Playlist");
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setDialogPane(view);
            Optional<ButtonType> clickedButton = dialog.showAndWait();
            if (clickedButton.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                playlistList.set(editingPlaylistIndex, controller.getPlaylist());
                playlistListTable.setItems(playlistList);
                loadPlaylistMetadata(playlistList.get(editingPlaylistIndex));
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
                t.join(150L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (metadataLoaderThread != null) {
            try {
                metadataLoaderThread.join(3000L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        playlistList.remove(currentlySelectedPlaylist);
        playlistListTable.setItems(playlistList);
        playlistListTable.getSelectionModel().clearSelection();
        songTableView.setItems(null);
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
    void loadPlaylistMetadata(Playlist p) {
        metadataLoaderThread = new Thread(() -> {
            Long begin = System.nanoTime();
            p.loadMetadata();
            Long end = System.nanoTime();
            System.out.println("Loading " + p.getPlaylistLength() + " songs took " + (end - begin) / 1000000 + " ms");
            songTableView.refresh();
        });
        metadataLoaderThread.start();
    }

    @FXML
    void handleCloseButton() {
        handleClose();
        System.exit(0);
    }


    void handleClose() {
        stop = true;
        if (t != null) {
            try {
                t.join(150L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (metadataLoaderThread != null) {
            try {
                metadataLoaderThread.join(3000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
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
                currentSongLabel.setText(getPlayingPlaylist().getCurrentSongName().substring(0, getPlayingPlaylist().getCurrentSongName().length() - 4));
                songTableView.getSelectionModel().clearSelection();
            });
            while (getPlayingPlaylist().isPlaying()) {
                if (stop) {
                    playing = false;
                    getPlayingPlaylist().stopPlayer();
                    return;
                }
                if (skip || jump || previous) {
                    getPlayingPlaylist().stopPlayer();
                    break;
                }
                if (updateProgressBar) {
                    Platform.runLater(() -> {
                        songProgressSlider.setValue(getPlayingPlaylist().getCurrentSongProgress());
                        secondsLabel.setText(String.format("%d:%02d",
                                (int)getPlayingPlaylist().getCurrentPlayingTimeSeconds() / 60,
                                (int)getPlayingPlaylist().getCurrentPlayingTimeSeconds() % 60));
                        durationLabel.setText(String.format("%d:%02d",
                                (int)getPlayingPlaylist().getCurrentSongLengthSeconds() / 60,
                                (int)getPlayingPlaylist().getCurrentSongLengthSeconds() % 60));
                        volumeLabel.setText(Long.toString(Math.round(volumeSlider.getValue())));
                    });
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (getPlayingPlaylist().getPlaylistLength() == 0) return;
            if (!jump) {
                getPlayingPlaylist().nextSong();
            }
        }
    }
}