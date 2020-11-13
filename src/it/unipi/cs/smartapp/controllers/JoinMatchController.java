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


public class JoinMatchController implements Controller {

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

        System.out.println("Join Match Controller done");
    }

    @FXML
    private void btnJoinMatchPressed(ActionEvent event) {
        // Check if the game name is valid
        String gameName = gameNameField.getText();
        if(gameName.isBlank()) {
            gameNameErrorLabel.setText("Invalid Game Name");
            return;
        }

        // Send JOIN request
        GameServerResponse res = gameServer.sendJOIN(gameName, stateMgr.getUsername(), 'H', "Test");
        if(res.code != ResponseCode.OK) {
            gameNameErrorLabel.setText("Cannot join the lobby");
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);

        // Update the state
        stateMgr.setInGame(gameName, false);

        // Change scene
        Renderer.getInstance().show("gameScene");
    }

    @FXML
    public void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    }
}
