package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Player;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


public class CellFactory implements Callback<TableColumn<Player, Image>, TableCell<Player, Image>> {

    private final EventHandler<MouseEvent> event;

    public CellFactory(EventHandler<MouseEvent> e) {
        this.event = e;
    }

    @Override
    public TableCell<Player, Image> call(TableColumn tableColumn) {
        final ImageView imageView = new ImageView();

        TableCell<Player, Image> cell = new TableCell<Player, Image>() {
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null) {
                    setText(item.toString());
                    imageView.setImage(new Image("it/unipi/cs/smartapp/sprites/barrier.png"));
                }
            }

            /*@Override
            protected void updateItem(Image item, boolean empty) {
                if (item != null) {
                    imageView.setImage(new Image("it/unipi/cs/smartapp/sprites/barrier.png", 30, 30, true, true));
                }
            }*/

            /*@Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    imageView.setImage(new Image("it/unipi/cs/smartapp/sprites/barrier.png"));
                }
            }*/
        };



        // Attach the imageview to the cell
        //cell.setGraphic(imageView);
        cell.setOnMouseClicked(event);
        return cell;
    }
}

