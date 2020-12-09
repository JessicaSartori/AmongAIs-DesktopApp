package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class resultController implements Controller {

    private StateManager stateManager;
    private TableManager table;

    @FXML
    private Label lblWinner, lblTitle;
    @FXML
    private TableView<Player> tblPlayers;

    public void initialize() {
        stateManager = StateManager.getInstance();
        table = new TableManager(tblPlayers);

        System.out.println("Result Controller done");
    }

    @Override
    public void updateContent() {
        lblTitle.setText(stateManager.getGameName() + " results!");
        table.createResult();
        Player firstPlayer = tblPlayers.getItems().get(0);
        lblWinner.setText("The winner is " + firstPlayer.getUsername());
    }

    @FXML
    private void btnGoBackPressed() {
        Controllers.closeGameServerConnection();
        Renderer.getInstance().show("mainMenu");
    }
}