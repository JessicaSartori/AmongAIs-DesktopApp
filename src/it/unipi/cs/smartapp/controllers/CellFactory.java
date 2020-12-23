package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.statemanager.Player;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


public class CellFactory implements Callback<TableColumn<Player, ImageView>, TableCell<Player, ImageView>> {

    private final EventHandler<MouseEvent> event;
    private final String imageName;

    public CellFactory(EventHandler<MouseEvent> e, String i) {
        this.event = e;
        this.imageName = i;
    }

    @Override
    public TableCell<Player, ImageView> call(TableColumn tableColumn) {
        final ImageView imageView = new ImageView();

        return new TableCell<Player, ImageView>() {
            @Override
            protected void updateItem(ImageView item, boolean empty) {
                super.updateItem(item, empty);

                if(!empty){
                    imageView.setImage(new Image("it/unipi/cs/smartapp/sprites/" + imageName));
                    setGraphic(imageView);
                    setOnMouseClicked(event);
                }
            }
        };
    }
}

