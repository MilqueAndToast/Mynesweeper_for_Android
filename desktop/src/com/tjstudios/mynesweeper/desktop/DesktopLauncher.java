package com.tjstudios.mynesweeper.desktop;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tjstudios.mynesweeper.Mynesweeper;
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Mynesweeper");
        config.setWindowedMode(400, 720);
        config.setResizable(false);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new Mynesweeper(), config);
    }
}
