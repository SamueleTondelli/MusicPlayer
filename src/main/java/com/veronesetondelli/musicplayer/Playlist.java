package com.veronesetondelli.musicplayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class Playlist{
    ObservableList<Song> songList;
    AudioPlayer player;
    int index;
    String name;

    public Playlist(String name) {
        index = 0;
        songList = FXCollections.observableArrayList();
        player = new AudioPlayer();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
        return songList.get(index).getName();
    }

    public double getCurrentSongProgress() { return (player.getPlayingTimeSeconds() / player.getSongLengthSeconds()) * 100; }

    public void setCurrentSongProgress(double progress) { player.setSongProgress(progress); }

    public ObservableList<String> getSongNames() {
        ObservableList<String> names = FXCollections.observableArrayList();
        for (Song s : songList) {
            names.add(s.getName());
        }
        return names;
    }

    public void swapSongs(int i1, int i2) {
        Song s1 = songList.get(i1);
        songList.set(i1, songList.get(i2));
        songList.set(i2, s1);
    }

    public int getPlaylistLength() { return songList.size(); }
}