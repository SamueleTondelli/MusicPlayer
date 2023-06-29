package com.veronesetondelli.musicplayer;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Playlist{
    List<String> songList;
    AudioPlayer player;
    int index;
    String name;

    public Playlist(String name) {
        songList = new ArrayList<String>();
        player = new AudioPlayer();
        this.name = name;
    }

    public void addSong(String name) {
        songList.add(name);
    }

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
}