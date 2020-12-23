package it.unipi.cs.smartapp.controllers;

import com.sun.glass.ui.EventLoop;
import it.unipi.cs.smartapp.statemanager.PlayerGameHistory;
import it.unipi.cs.smartapp.statemanager.StateManager;
import it.unipi.cs.smartapp.statemanager.Tournament;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class GameHistoryTable {

    private final TableView<PlayerGameHistory> tblHistory;
    private StateManager stateManager;

    public GameHistoryTable(TableView<PlayerGameHistory> tblView) {
        tblHistory = tblView;
        stateManager = StateManager.getInstance();
    }

    public void createTable() {
        // Link Table View with list of tournaments
        tblHistory.setItems(stateManager.gamesTableHistory);

        // Setup table columns
        TableColumn<PlayerGameHistory, String> nameCol = createColumn("Match", PlayerGameHistory.matchPropertyId, false);
        TableColumn<PlayerGameHistory, String> playerName = createColumn("Player Real Name", PlayerGameHistory.playerPropertyRealName, false);
        TableColumn<PlayerGameHistory, String> playerGameName = createColumn("Player Game Name", PlayerGameHistory.playerPropertyMatchName, false);
        TableColumn<PlayerGameHistory, String> playerScore = createColumn("Score", PlayerGameHistory.playerPropertyScore, false);
        TableColumn<PlayerGameHistory, String> playerKills = createColumn("# Kills", PlayerGameHistory.playerPropertyKills, false);
        TableColumn<PlayerGameHistory, String> playerAccuracy = createColumn("Accuracy", PlayerGameHistory.playerPropertyAccuracy, false);
        TableColumn<PlayerGameHistory, String> playerLeaderboard = createColumn("Leaderboard Position", PlayerGameHistory.playerPropertyLeaderboardPosition, false);

        tblHistory.getColumns().setAll(nameCol, playerName, playerGameName, playerScore, playerKills, playerAccuracy, playerLeaderboard);
    }

    private <P> TableColumn<PlayerGameHistory, P> createColumn(String name, String property, Boolean sortable) {
        TableColumn<PlayerGameHistory, P> column = new TableColumn<>(name);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setStyle("-fx-alignment: CENTER");

        // Substitute code for column.setReorderable(false) (did not work on some Java versions)
        Platform.runLater(() -> {
            for (Node header : tblHistory.lookupAll(".column-header")) {
                header.addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
            }
        });

        column.setSortable(sortable);
        if (sortable) column.setSortType(TableColumn.SortType.DESCENDING);

        return column;
    }
}
