package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;

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
    private void btnNewMatchPressed(ActionEvent event) {
        Renderer.getInstance().show("createMatch");
    }

    @FXML
    private void btnJoinMatchPressed(ActionEvent event) {
        Renderer.getInstance().show("joinMatch");
    }

    @FXML
    private void btnBackPressed(ActionEvent event) {
        Renderer.getInstance().show("login");
    }


}
