package it.unipi.cs.smartapp.screens;

import javafx.scene.Scene;
import javafx.stage.Stage;

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
     * Add a new scene
     */
    public void addScene(String name, Scene scene) {
        if(scenesMap.containsKey(name)) {
            System.err.println("screens.Renderer: Scene already exists");
            return;
        }

        scenesMap.put(name, scene);
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

}
