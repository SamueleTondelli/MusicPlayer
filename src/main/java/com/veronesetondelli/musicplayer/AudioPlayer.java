package com.veronesetondelli.musicplayer;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayer {
    private boolean playing;
    private Media media;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask task;

    AudioPlayer () { playing = false;}

    public void loadAudioFile(Song song) {
        try {
            media = song.getMedia();
            mediaPlayer = new MediaPlayer(media);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (!playing) {
            beginTimer();
            playing = true;
        }
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        cancelTimer();
        mediaPlayer.stop();
    }

    private void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                playing = true;
                if (mediaPlayer.getCurrentTime().toSeconds() >= media.getDuration().toSeconds()) {
                    cancelTimer();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 1000, 1000);
    }

    private void cancelTimer() {
        playing = false;
        timer.cancel();
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }

    public double getVolume() {
        return mediaPlayer.getVolume();
    }

    public double getPlayingTimeSeconds() { return mediaPlayer.getCurrentTime().toSeconds(); }

    public double getSongLengthSeconds() { return media.getDuration().toSeconds(); }

    public void setSongProgress(double progress) {
        mediaPlayer.seek(Duration.millis((media.getDuration().toMillis() * progress) / 100));
    }
}