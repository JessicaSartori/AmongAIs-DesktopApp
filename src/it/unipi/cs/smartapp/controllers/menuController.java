package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
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
        String username = stateMgr.getPrivateUsername();
        welcomeLabel.setText("Welcome back " + username + "!");
    }

    @FXML
    private void btnPLAYPressed() { Renderer.getInstance().show("joinMatch"); }

    @FXML
    private void btnCreatePressed() { Renderer.getInstance().show("createScene"); }

    @FXML
    private void btnBackPressed() { Renderer.getInstance().show("login"); }

    @FXML
    private void btnHowToPlayPressed() { Renderer.getInstance().show("howToPlayScene"); }

    @FXML
    private void btnGameSettings() { Renderer.getInstance().show("settings"); }

    @FXML
    private void btnTournamentsPressed() { Renderer.getInstance().show("tournaments");}

    @FXML
    private void btnHistoryPressed() { Renderer.getInstance().show("historyScene"); }
}
