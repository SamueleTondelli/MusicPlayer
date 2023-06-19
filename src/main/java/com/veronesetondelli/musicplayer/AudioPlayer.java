package com.veronesetondelli.musicplayer;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer implements LineListener, Runnable {
    private boolean playHasEnded;
    private Clip clip;
    boolean isPlaying = false;
    public AudioPlayer() {}

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            playHasEnded = true;
        }
    }

    public void loadAudioFile(String filename) {
        try {
            File f = new File(filename);
            AudioInputStream stream = AudioSystem.getAudioInputStream(f);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(stream);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        isPlaying = true;
        playHasEnded = false;
        clip.start();
        while(!playHasEnded) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        isPlaying = false;
    }
}
