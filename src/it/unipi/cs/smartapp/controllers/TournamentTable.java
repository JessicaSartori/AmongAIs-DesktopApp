package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Tournament;
import it.unipi.cs.smartapp.statemanager.TournamentStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import javax.security.auth.callback.Callback;

public class TournamentTable {
    private TournamentStatus tournamentStatus;

    private final TableView<Tournament> tblTournaments;

    public TournamentTable(TableView<Tournament> tblView) {
        tblTournaments = tblView;
        tournamentStatus = TournamentStatus.getInstance();
    }

    public void createTable() {
        // Link Table View with list of tournaments
        tblTournaments.setItems(tournamentStatus.tournamentTableList);

        // Setup table columns
        TableColumn<Tournament, String> nameCol = createColumn("Name", Tournament.tournamentPropertyName, false);
        TableColumn<Tournament, String> typeCol = createColumn("Type", Tournament.gamePropertyType, false);
        TableColumn<Tournament, String> startTournCol = createColumn("Starting", Tournament.startPropertyTournament, false);
        TableColumn<Tournament, String> endTournCol = createColumn("Ending", Tournament.endPropertyTournament, false);
        TableColumn<Tournament, String> startSubsCol = createColumn("Starting Subs", Tournament.startPropertySubscriptions, false);
        TableColumn<Tournament, String> endSubsCol = createColumn("Ending Subs", Tournament.endPropertySubscriptions, false);
        // TableColumn<Tournament, String> minPartCol = createColumn("Min Players", Tournament.minPropertyParticipants, false);
        // TableColumn<Tournament, String> maxPartCol = createColumn("Max Players", Tournament.maxPropertyParticipants, false);

        tblTournaments.getColumns().setAll(nameCol, typeCol, startTournCol, endTournCol, startSubsCol, endSubsCol);
    }

    private <P> TableColumn<Tournament, P> createColumn(String name, String property, Boolean sortable) {
        TableColumn<Tournament, P> column = new TableColumn<>(name);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setStyle("-fx-alignment: CENTER");

        // Substitute code for column.setReorderable(false) (did not work on some Java versions)
        Platform.runLater(() -> {
            for (Node header : tblTournaments.lookupAll(".column-header")) {
                header.addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
            }
        });

        column.setSortable(sortable);
        if (sortable) column.setSortType(TableColumn.SortType.DESCENDING);

        return column;
    }
}
