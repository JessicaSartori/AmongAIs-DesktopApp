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
    private void btnCreateMatchPressed(ActionEvent event) {
        String gameName = gamenameField.getText();
        if(gameName.isBlank()) {
            gamenameErrorLabel.setText("Invalid Game Name");
            return;
        }

        GameServerResponse res = gameServer.sendNEW(gameName);
        if(res.code == ResponseCode.ERROR) {
            gamenameErrorLabel.setText("Cannot create the match");
            System.err.println((String) res.get("freeText"));
            return;
        }
        System.out.println((String) res.get("freeText"));

        res = gameServer.sendJOIN(gameName, stateMgr.getUsername(), 'H', "Test");
        if(res.code == ResponseCode.ERROR) {
            gamenameErrorLabel.setText("Cannot join the lobby");
            System.err.println((String) res.get("freeText"));
            return;
        }
        System.out.println((String) res.get("freeText"));

        stateMgr.setCurrentGameName(gameName);
        stateMgr.setCreator(true);

        Renderer.getInstance().show("gameScene");
    }

    @FXML
    public void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    }
}
