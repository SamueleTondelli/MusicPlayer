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
import javafx.scene.media.Media;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;

public class Song {
    private final String fileLocation;
    private final String fileName;
    private String artist;
    private long duration;
    private boolean metadataHasBeenLoaded;

    /**
     * Constructor
     *
     * @param filePath of the song
     */
    public Song(String filePath) {
        fileName = Paths.get(filePath).getFileName().toString();
        fileLocation = filePath.substring(0, filePath.length() - 1 - fileName.length());
        metadataHasBeenLoaded = false;
    }

    public Media getMedia() {
        File f = new File(getFilePath());
        return new Media(f.toURI().toString());
    }

    public String getFilePath() {
        return fileLocation + System.getProperty("file.separator") + fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean getMetadataHasBeenLoaded() {
        return metadataHasBeenLoaded;
    }

    public String getLocation() {
        return fileLocation;
    }

    public String getArtist() {
        return metadataHasBeenLoaded ? artist : "NA";
    }

    public String getDurationFormatted() {
        if (metadataHasBeenLoaded) {
            return duration >= 0 ? String.format("%d:%02d", getDuration() / 60, getDuration() % 60) : "--:--";
        } else {
            return "NA";
        }
    }

    public long getDuration() {
        return duration;
    }

    /**
     * Loads File metadata with distinction between .wav and .mp3 files
     */
    public void loadMetadata() {
        if (fileName.endsWith("wav")) {
            duration = -1;
            artist = "Unknown";
            metadataHasBeenLoaded = true;
            return;
        }
        try (InputStream input = new FileInputStream(getFilePath())) {
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = new Mp3Parser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            duration = Long.parseLong(metadata.get("xmpDM:duration").split("\\.")[0]);
            artist = Arrays.asList(metadata.names()).contains("xmpDM:artist") ? metadata.get("xmpDM:artist") : "Unknown";
            metadataHasBeenLoaded = true;
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error loading metadata");
            alert.setHeaderText("Error loading metadata");
            alert.setContentText("Error loading metadata of " + getFilePath() + ".");
            alert.showAndWait();
        }
    }
}