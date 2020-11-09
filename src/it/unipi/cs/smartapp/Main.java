package it.unipi.cs.smartapp;

import it.unipi.cs.smartapp.controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import it.unipi.cs.smartapp.screens.Renderer;

import java.io.IOException;

public class Main extends Application {

    private Renderer r;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("AmongAIs");

        r = Renderer.createInstance(primaryStage);

        addScene("login", "/it/unipi/cs/smartapp/screens/loginScene.fxml");
        addScene("mainMenu", "/it/unipi/cs/smartapp/screens/mainMenuScene.fxml");
        addScene("createMatch", "/it/unipi/cs/smartapp/screens/createMatchScene.fxml");

        r.show("login");
        primaryStage.show();
    }

    // Add a scene (with its controller) to the Renderer
    private void addScene(String screenName, String FXMLFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLFile));
            Parent pane = loader.load();
            Scene scene = new Scene(pane, 800, 600);
            Controller sceneController = loader.getController();
            scene.setUserData(sceneController);
            r.addScene(screenName, scene);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    // Main
    public static void main(String[] args) { launch(args); }
}
