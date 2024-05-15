module blokus {
    requires javafx.controls;
    requires java.desktop;
    requires com.google.gson;
    exports blokus.render;
    exports blokus.logic;
    exports blokus.player;
    exports blokus.utils;
}