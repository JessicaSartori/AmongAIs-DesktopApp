package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class createGameController implements Controller {

    private StateManager stateMgr;
    private GameServerDriver gameServer;

    @FXML
    private TextField gamenameField;
    @FXML
    private Label gamenameErrorLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

        gamenameErrorLabel.setText("");

        System.out.println("Create Match Controller done");
    }

    @FXML
    private void createMatchBtnPressed(ActionEvent event) {
        String gamename = gamenameField.getText();
        if(gamename.isBlank()) {
            gamenameErrorLabel.setText("Gamename not valid");
            return;
        }

        String[] res = gameServer.sendNEW(gamename);
        if(!res[0].equals("OK")) {
            gamenameErrorLabel.setText("Cannot create the match");
            return;
        }

        res = gameServer.sendJOIN(gamename, stateMgr.getUsername(), 'H', "Test");
        if(!res[0].equals("OK")) {
            gamenameErrorLabel.setText("Cannot join the lobby");
            return;
        }

        String[] params = res[1].split(" ");

        stateMgr.setCurrentGameName(gamename);
        stateMgr.setTeam(Integer.parseInt(params[0]));
        stateMgr.setLoyalty(Integer.parseInt(params[1]));
    }

    @Override
    public void updateContent() {

    }
}
