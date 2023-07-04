package com.veronesetondelli.musicplayer;

import javafx.scene.media.Media;
import java.io.File;
import java.nio.file.Paths;

public class Song {
    private final String filePath;
    private double duration;
    public Song(String filePath) {
        this.filePath = filePath;
        try {
            duration = getMedia().getDuration().toSeconds();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Media getMedia() {
        File f = new File(filePath);
        return new Media(f.toURI().toString());
    }

    public double getDuration() {
        return duration;
    }

    public String getName() {
        return Paths.get(filePath).getFileName().toString();
    }
}