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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Playlist class
 */
public class Playlist {
    private final int NUMBER_LOADER_THREADS = 4;
    private final ObservableList<Song> songList;
    private final AudioPlayer player;
    /** Index of the selected song */
    private int index;
    private String name;

    /**
     * Constructor
     *
     * @param name of the playlist
     */
    public Playlist(String name) {
        index = 0;
        songList = FXCollections.observableArrayList();
        player = new AudioPlayer();
        this.name = name;
    }

    /**
     * Constructor
     *
     * @param p playlist to be constructed from
     */
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

    public ObservableList<Song> getSongList() {
        return songList;
    }

    public void addSong(String filePath) {
        songList.add(new Song(filePath));
    }

    public void addSongs(List<String> songs) {
        songs.forEach(s -> songList.add(new Song(s)));
    }

    public void removeSong(int index) throws IndexOutOfBoundsException {
        songList.remove(index);
    }

    public void playCurrentIndex() {
        player.play();
    }

    public void pause() {
        player.pause();
    }

    public void loadCurrentIndex() {
        player.loadAudioFile(songList.get(index));
    }

    public void nextSong() {
        index = (index + 1) % songList.size();
    }

    public void previousSong() {
        if ((getCurrentSongProgress()) > 50) {
            index = index == 0 ? songList.size() - 1 : index - 1;
        }
    }

    /**
     * Returns the current song progress
     *
     * @return progress between 0 and 100
     */
    public double getCurrentSongProgress() {
        return (player.getPlayingTimeSeconds() / player.getSongLengthSeconds()) * 100;
    }

    public void setCurrentSongProgress(double progress) {
        player.setSongProgress(progress);
    }

    public void setVolume(double volume) {
        player.setVolume(volume);
    }

    public String getCurrentSongName() {
        return songList.get(index).getFileName();
    }

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

    public int getPlaylistLength() {
        return songList.size();
    }

    /**
     * Loads all the songs' related metadata
     */
    public void loadMetadata() {
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_LOADER_THREADS);
        // Only the songs which haven't already been loaded get submitted to the executor service
        songList.stream().filter(s -> !s.getMetadataHasBeenLoaded()).forEach(s -> executor.execute(s::loadMetadata));
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                // Shows an alert in case the executor service gets timed out
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error loading metadata");
                alert.setHeaderText("Error loading metadata");
                alert.setContentText("Error loading metadata, timed out.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error loading metadata");
            alert.setHeaderText("Error loading metadata");
            alert.setContentText("Error loading metadata");
            alert.showAndWait();
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

    public String getArtist() {
        return songList.get(index).getArtist();
    }
}