package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.screens.Renderer;


public class joinController implements Controller {

    private StateManager stateMgr;
    private GameServerDriver gameServer;

    @FXML
    private TextField gameNameField;
    @FXML
    private Label errorLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

        errorLabel.setText("");

        System.out.println("Create Match Controller done");
    }

    @Override
    public void updateContent() { errorLabel.setText(""); }

    @FXML
    public void backBtnPressed() { Renderer.getInstance().show("mainMenu"); }

    @FXML
    private void btnCreatePressed() {
        String gameName = gameNameField.getText();
        errorLabel.setText("");

        if(isValid(gameName) && tryConnect() &&
                create(gameName) && join(gameName)) {

            stateMgr.setInGame(gameName, true);
            Renderer.getInstance().show("gameScene");
        }
    }

    @FXML
    private void btnJoinPressed() {
        String gameName = gameNameField.getText();
        errorLabel.setText("");

        if(isValid(gameName) && tryConnect() &&
                join(gameName)) {

            stateMgr.setInGame(gameName, true);
            Renderer.getInstance().show("gameScene");
        }
    }

    @FXML
    private void btnSpectatePressed() {
        String gameName = gameNameField.getText();
        errorLabel.setText("");
        if(!isValid(gameName) || !tryConnect()) return;

        // Check lobby existence
        GameServerResponse res = gameServer.sendSTATUS(gameName);
        switch (res.code) {
            case ERROR -> errorLabel.setText(res.freeText);
            case FAIL -> {
                System.err.println(res.freeText);
                errorLabel.setText("Cannot spectate the lobby");
            }
        }

        stateMgr.setInGame(gameName, false);
        Renderer.getInstance().show("spectateScene");
    }

    // Check if the game name is valid (no whitespaces)
    private boolean isValid(String s) {
        boolean valid = !(s.isBlank() || s.contains(" "));
        if(!valid) errorLabel.setText("Invalid Game Name");
        return valid;
    }

    // Try to open a connection to the game server
    // Return false if the connection fails, true otherwise
    private boolean tryConnect() {
        gameServer.openConnection();
        if (!gameServer.isConnected()) errorLabel.setText("Cannot connect to game server");
        return gameServer.isConnected();
    }

    // Send a NEW request and process the response
    // Return true if the lobby is created, false otherwise
    private boolean create(String gameName) {
        GameServerResponse res = gameServer.sendNEW(gameName);
        switch (res.code) {
            case ERROR -> errorLabel.setText(res.freeText);
            case FAIL -> {
                System.err.println(res.freeText);
                errorLabel.setText("Cannot create the lobby");
            }
        }
        return (res.code == ResponseCode.OK);
    }

    // Send a JOIN request and process the response
    // Return true if the request succeeded, false otherwise
    private boolean join(String gameName) {
        GameServerResponse res = gameServer.sendJOIN(gameName, stateMgr.getUsername(), 'H', "DesktopApp User");
        switch (res.code) {
            case ERROR -> errorLabel.setText(res.freeText);
            case FAIL -> {
                System.err.println(res.freeText);
                errorLabel.setText("Cannot join the lobby");
            }
        }
        return (res.code == ResponseCode.OK);
    }
}
