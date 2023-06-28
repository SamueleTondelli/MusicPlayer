package com.veronesetondelli.musicplayer;

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

    void addSong(String name) {
        songList.add(name);
    }

    void removeSong(String name) {
        songList.remove(name);
    }

    void removeSong(int index) throws IndexOutOfBoundsException{
        songList.remove(index);
    }

    void playCurrentIndex() {
        player.play();
    }

    void pause() {
        player.pause();
    }

    void loadCurrentIndex() {
        player.loadAudioFile(songList.get(index));
    }

    void nextSong() { index = (index + 1) % songList.size(); }
}
