package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.screens.Renderer;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class JoinMatchController implements Controller {

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

        System.out.println("Join Match Controller done");
    }

    @FXML
    private void joinMatchBtnPressed(ActionEvent event) {
        String gamename = gamenameField.getText();
        if(gamename.isBlank()) {
            gamenameErrorLabel.setText("Gamename not valid");
            return;
        }

        String[] res = gameServer.sendJOIN(gamename, stateMgr.getUsername(), 'H', "Test");
        if(!res[0].equals("OK")) {
            gamenameErrorLabel.setText(res[1]);
            return;
        }

        stateMgr.setCurrentGameName(gamename);

        System.out.println(res[1]);
    }

    @FXML
    public void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    }
}
