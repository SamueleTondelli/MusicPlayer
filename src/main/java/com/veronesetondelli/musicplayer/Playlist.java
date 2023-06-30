package com.veronesetondelli.musicplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Playlist{
    ObservableList<String> songList;
    AudioPlayer player;
    int index;
    String name;

    public Playlist(String name) {
        songList = FXCollections.observableArrayList();
        player = new AudioPlayer();
        this.name = name;
    }

    public void addSong(String name) {
        songList.add(name);
    }

    public void addSongs(List<String> songs) { songList.addAll(songs); }

    public void removeSong(String name) {
        songList.remove(name);
    }

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
        return Paths.get(songList.get(index)).getFileName().toString();
    }

    public double getCurrentSongProgress() { return (player.getPlayingTimeSeconds() / player.getSongLengthSeconds()) * 100; }

    public void setCurrentSongProgress(double progress) { player.setSongProgress(progress); }

    public ObservableList<String> getSongNames() {
        ObservableList<String> names = FXCollections.observableArrayList();
        for (String s : songList) {
            names.add(Paths.get(s).getFileName().toString());
        }
        return names;
    }
}