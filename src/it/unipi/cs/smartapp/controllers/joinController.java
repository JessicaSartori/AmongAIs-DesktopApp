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
    private void btnJoinPressed() {
        String gameName = gameNameField.getText();
        errorLabel.setText("");

        if(isValid(gameName) && tryConnect() &&
                join(gameName, 'H')) {

            stateMgr.setInGame(gameName, false, false);
            Renderer.getInstance().show("gameScene");
        }
    }

    @FXML
    private void btnSpectatePressed() {
        String gameName = gameNameField.getText();
        errorLabel.setText("");

        if(isValid(gameName) && tryConnect() &&
                join(gameName, 'O')) {

            stateMgr.setInGame(gameName, false, true);
            Renderer.getInstance().show("spectateScene");
        }
    }

    // Check if the game name is valid (no whitespaces)
    private boolean isValid(String s) {
        boolean valid = !(s.trim().isEmpty() || s.contains(" "));
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

    // Send a JOIN request and process the response
    // Return true if the request succeeded, false otherwise
    private boolean join(String gameName, Character nature) {
        GameServerResponse res = gameServer.sendJOIN(gameName, stateMgr.getUsername(), nature, stateMgr.getPrivateUsername());
        switch (res.code) {
            case ERROR:
                errorLabel.setText(res.freeText);
                break;
            case FAIL:
                System.err.println(res.freeText);
                if(nature == 'H') errorLabel.setText("Cannot join the lobby");
                else errorLabel.setText("Cannot spectate the lobby");
                break;
        }
        return (res.code == ResponseCode.OK);
    }
}
