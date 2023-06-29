module com.veronesetondelli.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    opens com.veronesetondelli.musicplayer to javafx.fxml;
    exports com.veronesetondelli.musicplayer;
}