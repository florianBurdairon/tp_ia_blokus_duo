package blokus;

import blokus.logic.Grid;
import blokus.player.Player;
import blokus.player.PlayerInterface;
import blokus.render.BlokusScene;

public class Main {
    public static void main(String[] args) {
        BlokusScene blokusScene = new BlokusScene();
        blokusScene.startApplication();
    }
}