package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

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
    private void btnGoBackPressed(ActionEvent event) {
        Renderer.getInstance().show("mainMenu");
    }
}
