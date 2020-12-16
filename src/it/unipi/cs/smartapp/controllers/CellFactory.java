package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Player;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


public class CellFactory implements Callback<TableColumn, TableCell> {

    private EventHandler event;

    public CellFactory(EventHandler e) { this.event = e; }

    @Override
    public TableCell call(TableColumn tableColumn) {
        TableCell cell = new TableCell(){
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(item.toString());
                }
            }
        };
        cell.setOnMouseClicked(event);
        return cell;
    }
}
