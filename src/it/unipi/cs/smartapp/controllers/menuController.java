package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.drivers.GameServerDriver;

public class menuController implements Controller {

    private StateManager stateMgr;
    private GameServerDriver gameServer;

    @FXML
    private Label welcomeLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();

        welcomeLabel.setText("");

        System.out.println("Main Menu Controller done");
    }

    @FXML
    private void newMatchBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("createMatch");
    }

    @FXML
    private void joinMatchBtnPressed(ActionEvent event) {
        System.out.println("Join game");
    }

    @FXML
    private void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("login");
    }

    @Override
    public void updateContent() {
        String username = stateMgr.getUsername();
        welcomeLabel.setText("Welcome back " + username + "!");
    }
}
