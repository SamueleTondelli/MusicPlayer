package com.veronesetondelli.musicplayer;

import javafx.scene.media.Media;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.audio.AudioParser;
import org.apache.tika.parser.mp3.Mp3Parser;
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
    public Song(String filePath) {
        fileName = Paths.get(filePath).getFileName().toString();
        fileLocation = filePath.substring(0, filePath.length() - 1 - fileName.length());
        metadataHasBeenLoaded = false;
    }

    public String getFilePath() { return fileLocation + System.getProperty("file.separator") + fileName; }
    public void loadMetadata(){
        try (InputStream input = new FileInputStream(getFilePath())) {
            ContentHandler handler = new DefaultHandler();
            Metadata metadata = new Metadata();
            Parser parser = fileName.endsWith("mp3") ? new Mp3Parser() : new AudioParser();
            ParseContext parseCtx = new ParseContext();
            parser.parse(input, handler, metadata, parseCtx);
            input.close();
            if (parser instanceof Mp3Parser) duration = Long.parseLong(metadata.get("xmpDM:duration").split("\\.")[0]);
            else duration = -1;
            artist = Arrays.asList(metadata.names()).contains("xmpDM:artist") ? metadata.get("xmpDM:artist") : "Unknown";
            metadataHasBeenLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Media getMedia() {
        File f = new File(fileLocation + System.getProperty("file.separator") + fileName);
        return new Media(f.toURI().toString());
    }

    public long getDuration() {
        return duration;
    }

    public String getFileName() {
        return fileName;
    }
    public boolean getMetadataHasBeenLoaded() { return metadataHasBeenLoaded; }
    public String getLocation() {return fileLocation;}
    public String getArtist() {return metadataHasBeenLoaded ? artist : "NA";}
    public String getDurationFormatted() {
        if (metadataHasBeenLoaded) {
            return duration >= 0 ? String.format("%d:%02d", getDuration() / 60, getDuration() % 60) : "--:--";
        }
        else {
            return "NA";
        }
    }
}