package it.unipi.cs.smartapp.screens;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.unipi.cs.smartapp.controllers.Controller;

public class Renderer {

    // Instance reference
    private static Renderer instance = null;

    // Make instance available to the outside
    public static Renderer getInstance() { return instance; }

    // Create the instance with the given Stage
    public static Renderer createInstance(Stage primaryStage) {
        instance = new Renderer(primaryStage);
        return instance;
    }

    /* ==============================
     * Instance variables and methods
     * ============================== */
    private Map<String, Scene> scenesMap;
    private Stage stage;

    private Renderer(Stage primaryStage) {
        stage = primaryStage;
        scenesMap = new HashMap<>();
    }

    /*
     * Add a new scene.
     * Load the scene from the given FXML file and associate it
     * with the given name
     */
    public void addScene(String name, String FXMLFile) {
        if(scenesMap.containsKey(name)) {
            System.err.println("screens.Renderer: Scene already exists");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLFile));
            Parent pane = loader.load();
            Scene scene = new Scene(pane, 800, 600);
            Controller sceneController = loader.getController();
            scene.setUserData(sceneController);
            scenesMap.put(name, scene);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Display the corresponding scene
     */
    public void show(String sceneName) {
        Scene sceneToShow = scenesMap.get(sceneName);
        Controller c = (Controller) sceneToShow.getUserData();
        c.updateContent();

        stage.setScene(sceneToShow);
    }

    /*
     * Make the stage visible
     */
    public void showStage() {
        stage.show();
    }

}
