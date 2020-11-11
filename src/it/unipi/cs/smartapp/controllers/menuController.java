package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.drivers.GameServerDriver;

public class menuController implements Controller {

    private StateManager stateMgr;

    @FXML
    private Label welcomeLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();

        welcomeLabel.setText("");

        System.out.println("Main Menu Controller done");
    }

    @Override
    public void updateContent() {
        String username = stateMgr.getUsername();
        welcomeLabel.setText("Welcome back " + username + "!");
    }

    @FXML
    private void newMatchBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("createMatch");
    }

    @FXML
    private void joinMatchBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("joinMatch");
    }

    @FXML
    private void backBtnPressed(ActionEvent event) {
        Renderer.getInstance().show("login");
    }


}
