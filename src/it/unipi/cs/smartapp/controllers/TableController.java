package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class TableController {
    // Singleton components
    private final StateManager stateMgr;
    private final GameServerDriver gameServer;

    private final TableView<Player> tblPlayers;


    public TableController(TableView<Player> tblView) {
        tblPlayers = tblView;
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
    }

    public void createTable(Boolean playing, Label lblResponse) {
        // Link Table View with list of players
        tblPlayers.setItems(stateMgr.playersList);

        // Setup data table columns
        TableColumn<Player, String> usernameCol = createColumn("Name", Player.usernamePropertyName, false);
        TableColumn<Player, String> stateCol = createColumn("Status", Player.statePropertyName, false);

        tblPlayers.getColumns().setAll(usernameCol, stateCol);

        if(playing) {
            // Setup voting table columns
            TableColumn<Player, Image> accuse = createColumn("Accuse", Player.accusePropertyName, false);
            TableColumn<Player, Image> judgeHuman = createColumn("Human", Player.judgeHumanPropertyName, false);
            TableColumn<Player, Image> judgeAI = createColumn("AI", Player.judgeAIPropertyName, false);

            tblPlayers.getColumns().add(accuse);
            tblPlayers.getColumns().add(judgeHuman);
            tblPlayers.getColumns().add(judgeAI);

            accuse.setCellFactory(new CellFactory(event -> {
                Player p = tblPlayers.getSelectionModel().getSelectedItem();
                GameServerResponse response = gameServer.sendACCUSE(stateMgr.getGameName(), p.getUsername());
                updateResponse(response, lblResponse, "You accused " + p.getUsername() + "!");
            }));

            judgeHuman.setCellFactory(new CellFactory(event -> {
                Player p = tblPlayers.getSelectionModel().getSelectedItem();
                GameServerResponse response = gameServer.sendJUDGE(stateMgr.getGameName(), p.getUsername(), "H");
                updateResponse(response, lblResponse, "You judged " + p.getUsername() + " as human!");
            }));

            judgeAI.setCellFactory(new CellFactory(event -> {
                Player p = tblPlayers.getSelectionModel().getSelectedItem();
                GameServerResponse response = gameServer.sendJUDGE(stateMgr.getGameName(), p.getUsername(), "AI");
                updateResponse(response, lblResponse, "You judged " + p.getUsername() + " as AI!");
            }));
        }

        tblPlayers.setRowFactory(tv -> new TableRow<Player>() {
            @Override
            protected void updateItem(Player p, boolean empty) {
                for (Player element : stateMgr.playersList) {
                    System.out.println("username " + element.getUsername() + " team " + element.getTeam());
                }
                System.out.println("--------------------");

                super.updateItem(p, empty);
                if (p == null || p.getTeam() == null) {
                    setStyle("");
                    return;
                } else if (p.getTeam() == 0) {
                    setStyle("-fx-background-color: #fc6262;");
                    //System.out.println("Should be 0 RED " + p.getTeam());
                    } else {
                    setStyle("-fx-background-color: #7a91ff;");
                    //    System.out.println("Should be 1 BLUE " + p.getTeam());
                }

                if (p.getUsername().equals(stateMgr.getUsername())) setStyle(getStyle() + "-fx-font-weight: 800");

            }
        });
    }

    public void createResult() {
        // Setup table and default columns
        createTable(false, null);

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

    private void updateResponse(GameServerResponse response, Label lblResponse, String okText) {
        switch (response.code) {
            case FAIL:
                System.err.println(response.freeText);
                return;
            case ERROR:
                lblResponse.setTextFill(Color.RED);
                lblResponse.setText(response.freeText);
                break;
            case OK:
                lblResponse.setTextFill(Color.DARKGREEN);
                lblResponse.setText(okText);
        }
        labelFader(lblResponse, 2.0).play();
    }

    protected FadeTransition labelFader(Node node, Double seconds) {
        FadeTransition fade = new FadeTransition(Duration.seconds(seconds), node);
        fade.setFromValue(1);
        fade.setToValue(0);

        return fade;
    }
}

