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

public class MusicPlayerController implements Runnable {

    ObservableList<Playlist> playlistList;
    @FXML
    private Button muteBtn;
    @FXML
    private Button playBtn;
    @FXML
    private Label volumeLabel;
    private int currentlyPlayingPlaylist;
    private int currentlySelectedPlaylist;
    private boolean playing;
    private boolean skip;
    private boolean previous;
    private boolean stop;
    private boolean mute;
    private boolean updateSongProgress;
    private boolean jump;
    private boolean updateSongData;
    private double currVol;
    private Thread t;
    private Thread metadataLoaderThread;
    private Image imagePause;
    private Image imagePlay;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label currentSongLabel;
    @FXML
    private Label secondsLabel;
    @FXML
    private Label durationLabel;
    @FXML
    private Label currentArtistLabel;
    @FXML
    private Label currentPlaylistLabel;
    @FXML
    private Slider songProgressSlider;
    @FXML
    private ImageView coverArtView;
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
        updateSongProgress = true;
        jump = false;

        imagePause = new Image(getClass().getResourceAsStream("pause-ret.png"));
        imagePlay = new Image(getClass().getResourceAsStream("play-button-ret.png"));

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
            if (newValue.intValue() == -1)
                return;
            if (currentlyPlayingPlaylist == -1) {
                currentlyPlayingPlaylist = currentlySelectedPlaylist;
                getSelectedPlaylist().setIndex(newValue.intValue());
                t = new Thread(this);
                t.start();
            } else if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
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
        volumeLabel.setText("100");
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!stop) {
                getPlayingPlaylist().setVolume(newValue.doubleValue() * 0.01);
            }
            volumeLabel.setText(Long.toString(Math.round(newValue.doubleValue())));
        });
    }

    Playlist getSelectedPlaylist() {
        return playlistList.get(currentlySelectedPlaylist);
    }

    Playlist getPlayingPlaylist() {
        return playlistList.get(currentlyPlayingPlaylist);
    }

    @FXML
    void onPlayButtonClick() {
        if (stop)
            return;
        if (playing) {
            getPlayingPlaylist().pause();
            playBtn.setGraphic(new ImageView(imagePause));
            playing = false;
        } else {
            getPlayingPlaylist().playCurrentIndex();
            playBtn.setGraphic(new ImageView(imagePlay));
            playing = true;
        }
    }

    @FXML
    void onSkipButtonPress() {
        if (!stop) {
            skip = true;
        }
    }

    @FXML
    void onPreviousButtonPress() {
        if (!stop) {
            previous = true;
            getPlayingPlaylist().previousSong();
        }
    }

    @FXML
    void handleCreatePlaylist() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("edit-view.fxml"));
            DialogPane view = loader.load();
            EditPlaylistController controller = loader.getController();

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
    void handleLoadAsJSON() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json"));

            File file = chooser.showOpenDialog(null);
            if (file != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                List<String> songs = mapper.readValue(file, new TypeReference<>() {
                });
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
        if (playlistListTable.getSelectionModel().getSelectedIndex() == -1)
            return;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("edit-view.fxml"));
            DialogPane view = loader.load();
            EditPlaylistController controller = loader.getController();

            int editingPlaylistIndex = currentlySelectedPlaylist;
            if (editingPlaylistIndex == currentlyPlayingPlaylist) {
                playlistListTable.getSelectionModel().clearSelection();
                songTableView.setItems(null);
                stopPlay();
            }
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

    public void stopPlay() {
        stop = true;
        if (t != null) {
            try {
                t.join(150L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateSongProgress = false;
        updateSongData = false;
        currentlyPlayingPlaylist = -1;
        currentArtistLabel.setText(" ");
        currentPlaylistLabel.setText(" ");
        currentSongLabel.setText(" ");
        coverArtView.setImage(null);
        songProgressSlider.setValue(0.0);
        secondsLabel.setText(String.format("%d:%02d", 0, 00));
        durationLabel.setText(String.format("%d:%02d", 0, 00));
    }

    @FXML
    void onRemovePlaylistButtonPress() {
        if (playlistList.size() == 0 || currentlySelectedPlaylist == -1)
            return;
        if (currentlySelectedPlaylist == currentlyPlayingPlaylist) {
            stopPlay();
        }
        if (metadataLoaderThread != null) {
            try {
                metadataLoaderThread.join(3000L);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        playlistList.remove(currentlySelectedPlaylist);
        if (currentlyPlayingPlaylist > currentlySelectedPlaylist)
            currentlyPlayingPlaylist--;
        playlistListTable.setItems(playlistList);
        if (currentlyPlayingPlaylist > 0)
            playlistListTable.getSelectionModel().select(currentlyPlayingPlaylist);
        else
            playlistListTable.getSelectionModel().clearSelection();
        songTableView.getSelectionModel().clearSelection();
        songTableView.setItems(currentlySelectedPlaylist > 0 ? getPlayingPlaylist().getSongList() : null);
    }

    @FXML
    void onMuteButtonClick() {
        if (!stop) {
            if (!mute) {
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
    }

    @FXML
    void handleSongProgressChange() {
        if (!stop) {
            getPlayingPlaylist().setCurrentSongProgress(songProgressSlider.getValue());
        }
        updateSongProgress = true;
    }

    @FXML
    void onSongProgressSliderPress() {
        updateSongProgress = false;
    }

    @FXML
    void handleCloseButton() {
        handleClose();
        System.exit(0);
    }

    void handleClose() {
        stopPlay();

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
            updateSongData = true;
            getPlayingPlaylist().setVolume(volumeSlider.getValue() * 0.01);

            Platform.runLater(() -> {
                playBtn.setGraphic(new ImageView(imagePlay));
                currentSongLabel.setText(getPlayingPlaylist().getCurrentSongName().substring(0, getPlayingPlaylist().getCurrentSongName().length() - 4));
                currentPlaylistLabel.setText(getPlayingPlaylist().getName());
                currentArtistLabel.setText(getPlayingPlaylist().getArtist());
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

                Platform.runLater(() -> {
                    if (updateSongProgress) {
                        songProgressSlider.setValue(getPlayingPlaylist().getCurrentSongProgress());
                        secondsLabel.setText(String.format("%d:%02d", (int) getPlayingPlaylist().getCurrentPlayingTimeSeconds() / 60, (int) getPlayingPlaylist().getCurrentPlayingTimeSeconds() % 60));
                    }
                    if (updateSongData && getPlayingPlaylist().isPlayerReady()) {
                        durationLabel.setText(String.format("%d:%02d", (int) getPlayingPlaylist().getCurrentSongLengthSeconds() / 60, (int) getPlayingPlaylist().getCurrentSongLengthSeconds() % 60));
                        coverArtView.setImage(getPlayingPlaylist().getPlayer().getCover());
                        updateSongData = false;
                    }
                });

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (getPlayingPlaylist().getPlaylistLength() == 0)
                return;
            if (!jump) {
                getPlayingPlaylist().nextSong();
            }
        }
    }
}