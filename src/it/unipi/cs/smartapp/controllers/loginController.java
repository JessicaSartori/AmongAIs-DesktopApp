package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.screens.Renderer;


public class loginController implements Controller {

    private StateManager stateMgr;

    @FXML
    private TextField usernameField;
    @FXML
    private Label errorLabel;

    public void initialize() {
        stateMgr = StateManager.getInstance();

        errorLabel.setText("");

        System.out.println("Login Controller done");
    }

    @Override
    public void updateContent() {
        String username = stateMgr.getUsername();
        if(username != null) {
            usernameField.setText(username);
        }
    }

    @FXML
    private void btnPlayAsGuestPressed(ActionEvent event) {
        String username = usernameField.getText();

        if(username.isBlank() || username.contains(" ")) {
            errorLabel.setText("Username not valid");
            return;
        }

        errorLabel.setText("");

        stateMgr.setUsername(username);
        Renderer.getInstance().show("mainMenu");
    }
}