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

import javafx.scene.control.Alert;
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error");
            alert.setHeaderText("Error: loading audio file");
            alert.setContentText("Error: couldn't load audio file " + song.getFilePath() + ".");
            alert.showAndWait();
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