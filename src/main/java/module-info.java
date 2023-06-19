module com.veronesetondelli.musicplayer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.veronesetondelli.musicplayer to javafx.fxml;
    exports com.veronesetondelli.musicplayer;
}