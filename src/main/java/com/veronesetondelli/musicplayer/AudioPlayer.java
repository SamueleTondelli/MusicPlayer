package com.veronesetondelli.musicplayer;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayer {
    private boolean playing;
    private boolean playerIsReady;
    private Media media;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask task;

    AudioPlayer() {
        playing = false;
        playerIsReady = false;
    }

    public double getVolume() {
        return mediaPlayer.getVolume();
    }

    public void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }

    public double getPlayingTimeSeconds() {
        return mediaPlayer.getCurrentTime().toSeconds();
    }

    public double getSongLengthSeconds() {
        return media.getDuration().toSeconds();
    }

    public void setSongProgress(double progress) {
        mediaPlayer.seek(Duration.millis((media.getDuration().toMillis() * progress) / 100));
    }

    /**
     * Returns the coverArt of a media from metadata
     *
     * @return Image
     */
    public Image getCover() {
        return (Image) media.getMetadata().get("image");
    }

    /**
     * Creates a mediaPlayer from song and setting on ready
     */
    public void loadAudioFile(Song song) {
        try {
            playerIsReady = false;
            media = song.getMedia();
            if (mediaPlayer != null)
                mediaPlayer.dispose();
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnReady(() -> playerIsReady = true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets playing to true and calling beginTimer(), mediaPlayer is playing
     */
    public void play() {
        if (!playing) {
            beginTimer();
            playing = true;
        }
        mediaPlayer.play();
    }

    /**
     * Starts Timer for controlling when the mediaPlayer ends playing the media
     */
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

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        cancelTimer();
        mediaPlayer.stop();
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isPlayerReady() {
        return playerIsReady;
    }
}