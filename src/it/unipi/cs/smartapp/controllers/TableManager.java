package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class TableManager {

    private final StateManager stateMgr;

    private TableView<Player> tblPlayers;

    public TableManager(TableView<Player> tblView) {
        tblPlayers = tblView;
        stateMgr = StateManager.getInstance();
    }

    public void createTable(){
        // Link Table View with list of players
        tblPlayers.setItems(stateMgr.playersList);

        // Setup table columns
        TableColumn<Player, Integer> teamCol = new TableColumn<>("Team");
        teamCol.setCellValueFactory(new PropertyValueFactory<>(stateMgr.playersList.get(0).teamProperty().getName()));
        teamCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Player, String> usernameCol = new TableColumn<>("Name");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>(stateMgr.playersList.get(0).usernameProperty().getName()));
        usernameCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Player, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>(stateMgr.playersList.get(0).scoreProperty().getName()));
        scoreCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Player, String> stateCol = new TableColumn<>("Status");
        stateCol.setCellValueFactory(new PropertyValueFactory<>(stateMgr.playersList.get(0).stateProperty().getName()));
        stateCol.setStyle("-fx-alignment: CENTER;");

        tblPlayers.getColumns().setAll(teamCol, usernameCol, scoreCol, stateCol);
    }
}
