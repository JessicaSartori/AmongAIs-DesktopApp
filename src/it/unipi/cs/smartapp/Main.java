package it.unipi.cs.smartapp;

import javafx.application.Application;
import javafx.stage.Stage;

import it.unipi.cs.smartapp.screens.Renderer;

public class Main extends Application {

    private Renderer r;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AmongAIs");

        r = Renderer.createInstance(primaryStage);

        r.addScene("login", "/it/unipi/cs/smartapp/screens/loginScene.fxml");
        r.addScene("mainMenu", "/it/unipi/cs/smartapp/screens/mainMenuScene.fxml");
        r.addScene("createMatch", "/it/unipi/cs/smartapp/screens/createMatchScene.fxml");
        r.addScene("joinMatch", "/it/unipi/cs/smartapp/screens/joinMatchScene.fxml");

        // Added temporarily for testing (to merge with in game view)
        r.addScene("map", "/it/unipi/cs/smartapp/screens/mapScene.fxml");

        r.show("login");
        r.showStage();
    }

    // Main
    public static void main(String[] args) { launch(args); }
}
