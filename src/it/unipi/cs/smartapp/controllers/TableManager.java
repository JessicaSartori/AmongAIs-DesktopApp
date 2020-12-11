package it.unipi.cs.smartapp.controllers;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.scene.input.MouseEvent;


public class TableManager {

    private final StateManager stateMgr;

    private final TableView<Player> tblPlayers;


    public TableManager(TableView<Player> tblView) {
        tblPlayers = tblView;
        stateMgr = StateManager.getInstance();
    }

    public void createTable() {
        // Link Table View with list of players
        tblPlayers.setItems(stateMgr.playersList);

        // Setup table columns
        TableColumn<Player, Integer> teamCol = createColumn("Team", Player.teamPropertyName, false);
        TableColumn<Player, String> usernameCol = createColumn("Name", Player.usernamePropertyName, false);
        TableColumn<Player, String> stateCol = createColumn("Status", Player.statePropertyName, false);

        tblPlayers.getColumns().setAll(teamCol, usernameCol, stateCol);
    }

    public void createResult() {
        // Setup table and default columns
        createTable();

        // Add score column
        TableColumn<Player, Integer> scoreCol = createColumn("Score", Player.scorePropertyName, true);
        tblPlayers.getColumns().add(scoreCol);
        tblPlayers.getSortOrder().add(scoreCol);
    }

    private <P> TableColumn<Player, P> createColumn(String name, String property, Boolean sortable) {
        TableColumn<Player, P> column = new TableColumn<>(name);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setStyle("-fx-alignment: CENTER");

        // Substitute code for column.setReorderable(false) (did not work on some Java versions)
        Platform.runLater(() -> {
            for (Node header : tblPlayers.lookupAll(".column-header")) {
                header.addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
            }
        });

        column.setSortable(sortable);
        if (sortable) column.setSortType(TableColumn.SortType.DESCENDING);

        return column;
    }
}
