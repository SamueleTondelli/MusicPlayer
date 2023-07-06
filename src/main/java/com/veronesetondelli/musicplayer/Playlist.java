package com.veronesetondelli.musicplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Playlist{
    private ObservableList<Song> songList;
    private AudioPlayer player;
    private int index;
    private String name;
    private final int NUMBER_LOADER_THREADS = 4;

    public Playlist(String name) {
        index = 0;
        songList = FXCollections.observableArrayList();
        player = new AudioPlayer();
        this.name = name;
    }

    public Playlist(Playlist p) {
        index = 0;
        name = p.name;
        songList = FXCollections.observableArrayList(p.songList);
        player = new AudioPlayer();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public ObservableList<Song> getSongList() {return songList; }

    public void addSong(String filePath) {
        songList.add(new Song(filePath));
    }

    public void addSongs(List<String> songs) { songs.forEach(s -> songList.add(new Song(s)));}

    public void removeSong(int index) throws IndexOutOfBoundsException{ songList.remove(index); }

    public void removeAllSongs() { songList.clear(); }

    public void playCurrentIndex() { player.play(); }

    public void pause() { player.pause(); }

    public void loadCurrentIndex() {
        player.loadAudioFile(songList.get(index));
    }
    public void nextSong() { index = (index + 1) % songList.size(); }
    public void previousSong() {
        if((getCurrentSongProgress()) > 50) {
            index = index == 0 ? songList.size() - 1 : index - 1;
        }
    }

    public void setVolume(double volume) {
        player.setVolume(volume);
    }

    public double getVolume() {
        return player.getVolume();
    }

    public String getCurrentSongName() {
        return songList.get(index).getFileName();
    }

    public double getCurrentSongProgress() { return (player.getPlayingTimeSeconds() / player.getSongLengthSeconds()) * 100; }

    public void setCurrentSongProgress(double progress) { player.setSongProgress(progress); }

    public ObservableList<String> getSongNames() {
        ObservableList<String> names = FXCollections.observableArrayList();
        for (Song s : songList) {
            names.add(s.getFileName());
        }
        return names;
    }

    public void swapSongs(int i1, int i2) {
        Song s1 = songList.get(i1);
        songList.set(i1, songList.get(i2));
        songList.set(i2, s1);
    }

    public int getPlaylistLength() { return songList.size(); }
    public void loadMetadata() {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_LOADER_THREADS);
        songList.stream().filter(s -> !s.getMetadataHasBeenLoaded()).forEach(s -> executor.execute(s::loadMetadata));
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error loading metadata");
                alert.setHeaderText("Error loading metadata");
                alert.setContentText("Error loading metadata, timed out.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<String> getSongsPathList() {
        List<String> l = new ArrayList<>();
        songList.forEach(s -> l.add(s.getFilePath()));
        return l;
    }



    public void stopPlayer() {
        player.stop();
    }

    public double getCurrentPlayingTimeSeconds() {
        return player.getPlayingTimeSeconds();
    }

    public double getCurrentSongLengthSeconds() {
        return player.getSongLengthSeconds();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isPlayerReady() {
        return player.isPlayerReady();
    }
    public AudioPlayer getPlayer() {
        return player;
    }
}