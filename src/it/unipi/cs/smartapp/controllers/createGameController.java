package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.screens.Renderer;


public class createGameController implements Controller {

    private StateManager stateMgr;
    private GameServerDriver gameServer;

    @FXML
    private TextField gameNameField;
    @FXML
    private Label gameNameErrorLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

        gameNameErrorLabel.setText("");

        System.out.println("Create Match Controller done");
    }

    @FXML
    private void btnCreateMatchPressed(ActionEvent event) {
        // Check if the game name is valid
        String gameName = gameNameField.getText();
        if(gameName.isBlank()) {
            gameNameErrorLabel.setText("Invalid Game Name");
            return;
        }

        // Open connection to Game Server
        gameServer.openConnection();
        if(!gameServer.isConnected()) {
            gameNameErrorLabel.setText("Cannot connect to server");
            return;
        }

        // Send NEW request
        GameServerResponse res = gameServer.sendNEW(gameName);
        if(res.code != ResponseCode.OK) {
            gameNameErrorLabel.setText("Cannot create the match");
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);

        // Send JOIN request
        res = gameServer.sendJOIN(gameName, stateMgr.getUsername(), 'H', "Test");
        if(res.code != ResponseCode.OK) {
            gameNameErrorLabel.setText("Cannot join the lobby");
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);

        // Update state
        stateMgr.setInGame(gameName, true);

        // Change scene
        Renderer.getInstance().show("gameScene");
    }

    @FXML
    public void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    }
}
