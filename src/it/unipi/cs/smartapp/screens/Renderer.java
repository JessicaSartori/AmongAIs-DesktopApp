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
    public static Renderer createInstance(Stage primaryStage, double w, double h) {
        instance = new Renderer(primaryStage, w, h);
        return instance;
    }


    /*
     * ******************************
     * Instance variables and methods
     * ******************************
     */
    private final Map<String, Parent> scenesMap;
    private final Map<String, Controller> controllersMap;
    private final Stage stage;
    double width, height;

    private Renderer(Stage primaryStage, double w, double h) {
        width = w;
        height = h;
        stage = primaryStage;

        scenesMap = new HashMap<>();
        controllersMap = new HashMap<>();

        stage.setMaximized(true);
    }

    // Add a new scene.
    // Load the scene root from the given FXML file and associate it with the given name
    public void addScene(String name, String FXMLFile) {
        if(scenesMap.containsKey(name)) {
            System.err.println("screens.Renderer: Scene already exists");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXMLFile));
            Parent root = loader.load();
            scenesMap.put(name, root);
            Controller sceneController = loader.getController();
            controllersMap.put(name, sceneController);

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

     // Display the scene corresponding to the given name
    public void show(String sceneName) {
        Parent sceneRoot = scenesMap.get(sceneName);
        Controller rootController = controllersMap.get(sceneName);
        rootController.updateContent();

        try {
            stage.getScene().setRoot(sceneRoot);
        } catch (NullPointerException e) {
            stage.setScene(new Scene(sceneRoot, width, height));
        }
    }

    // Make the stage visible
    public void showStage() { stage.show(); }
}
