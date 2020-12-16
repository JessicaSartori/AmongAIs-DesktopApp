package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import it.unipi.cs.smartapp.statemanager.Player;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


public class TableManager {

    private final StateManager stateMgr;
    private final GameServerDriver gameServer;

    private final TableView<Player> tblPlayers;


    public TableManager(TableView<Player> tblView) {
        tblPlayers = tblView;
        stateMgr = StateManager.getInstance();
        gameServer = GameServerDriver.getInstance();
    }

    public void createTable(Boolean playing) {
        // Link Table View with list of players
        tblPlayers.setItems(stateMgr.playersList);

        // Setup data table columns
        //TableColumn<Player, Integer> teamCol = createColumn("Team", Player.teamPropertyName, false);
        TableColumn<Player, String> usernameCol = createColumn("Name", Player.usernamePropertyName, false);
        TableColumn<Player, String> stateCol = createColumn("Status", Player.statePropertyName, false);

        tblPlayers.getColumns().setAll(usernameCol, stateCol);

        if(playing) {
            // Setup voting table columns
            TableColumn<Player, Button> accuse = createColumn("Accuse", Player.accusePropertyName, false);
            TableColumn<Player, Button> judgeHuman = createColumn("Human", Player.judgeHumanPropertyName, false);
            TableColumn<Player, Button> judgeAI = createColumn("AI", Player.judgeAIPropertyName, false);

            tblPlayers.getColumns().add(accuse);
            tblPlayers.getColumns().add(judgeHuman);
            tblPlayers.getColumns().add(judgeAI);

            accuse.setCellFactory(new Callback<TableColumn<Player, Button>, TableCell<Player, Button>>() {
                @Override
                public TableCell<Player, Button> call(TableColumn<Player, Button> tableColumn) {
                    TableCell cell = new TableCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item != null) {
                                setText(item.toString());
                            }
                        }
                    };
                    cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            //get the player name + send accuse
                            //check if player is in same team, otw write error
                            System.out.println("Clicked on accuse cell! Id: " + cell.getId());
                        }
                    });
                    return cell;
                }
            });

            judgeHuman.setCellFactory(new Callback<TableColumn<Player, Button>, TableCell<Player, Button>>() {
                @Override
                public TableCell<Player, Button> call(TableColumn<Player, Button> tableColumn) {
                    TableCell cell = new TableCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item != null) {
                                setText(item.toString());
                            }
                        }
                    };
                    cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            //get the player name + send accuse
                            //check if player is in same team, otw write error
                            System.out.println("Clicked on judge human cell! Id: " + cell.getId());

                        }
                    });
                    return cell;
                }
            });

            judgeAI.setCellFactory(new Callback<TableColumn<Player, Button>, TableCell<Player, Button>>() {
                @Override
                public TableCell<Player, Button> call(TableColumn<Player, Button> tableColumn) {
                    TableCell cell = new TableCell(){
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if(item != null) {
                                setText(item.toString());
                            }
                        }
                    };
                    cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent mouseEvent) {
                            //get the player name + send judge

                            Player p = tblPlayers.getSelectionModel().getSelectedItem();

                            System.out.println("Clicked on judge AI cell! Id: "+p.getUsername());
                        }
                    });
                    return cell;
                }
            });
        }
    }

    public void createResult() {
        // Setup table and default columns
        createTable(false);

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

