module com.veronesetondelli.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.veronesetondelli.musicplayer to javafx.fxml;
    exports com.veronesetondelli.musicplayer;
}