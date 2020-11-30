package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.screens.Renderer;


public class loginController implements Controller {

    private StateManager stateMgr;

    @FXML
    private TextField usernameField, publicUsernameField;
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
    private void btnPlayAsGuestPressed() {
        String privateUsername = usernameField.getText();
        String publicUsername = publicUsernameField.getText();

        if(privateUsername.isBlank() || privateUsername.contains(" ")) {
            errorLabel.setText("Username not valid");
            return;
        }

        if(publicUsername.isBlank()) publicUsername = privateUsername;
        else if(publicUsername.contains(" ")) {
            errorLabel.setText("Public username not valid");
            return;
        }

        errorLabel.setText("");

        stateMgr.setPrivateUsername(privateUsername);
        stateMgr.setUsername(publicUsername);
        Renderer.getInstance().show("mainMenu");
    }

}