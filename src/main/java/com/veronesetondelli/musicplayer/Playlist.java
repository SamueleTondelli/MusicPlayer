package com.veronesetondelli.musicplayer;

import java.util.ArrayList;
import java.util.List;

public class Playlist implements Runnable{
    List<String> songList;
    AudioPlayer player;
    boolean playing;
    boolean skip;
    boolean stop;
    int index;
    String name;

    public Playlist(String name) {
        songList = new ArrayList<String>();
        player = new AudioPlayer();
        this.name = name;
        stop = true;
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
        playing = true;
        player.play();
    }

    void pause() {
        playing = false;
        player.stop();
    }

    void loadCurrentIndex() {
        player.loadAudioFile(songList.get(index));
    }

    @Override
    public void run() {
        loadCurrentIndex();
        playCurrentIndex();
        stop = false;
        while (true) {
            skip = false;
            while (!player.playHasEnded) {
                if (stop) {
                    playing = false;
                    player.close();
                    return;
                }
                if (skip) {
                    player.stop();
                    break;
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            player.close();
            index = (index + 1) % songList.size();
            loadCurrentIndex();
            playCurrentIndex();
        }
    }
}
