package com.veronesetondelli.musicplayer;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer implements LineListener {
    boolean playHasEnded;
    private Clip clip;
    FloatControl volumeControl;
    public AudioPlayer() {}

    @Override
    public void update(LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP && clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
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
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(volumeControl.getMaximum());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void close() {
        stop();
        clip.close();
    }

    public void play() {
        playHasEnded = false;
        clip.start();
    }

    public void stop() {
        clip.stop();
    }
}
