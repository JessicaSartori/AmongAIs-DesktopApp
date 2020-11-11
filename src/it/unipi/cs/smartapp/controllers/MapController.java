package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.statemanager.StateManager;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;

import java.awt.*;


public class MapController implements Controller {

    private StateManager stateManager;
    private GameServerDriver gameServerDriver;

    @FXML
    private Canvas mapCanvas;
    private GraphicsContext canvasContext;

    public void initialize() {
        stateManager = StateManager.getInstance();
        gameServerDriver = GameServerDriver.getInstance();

        canvasContext = mapCanvas.getGraphicsContext2D();

        System.out.println("Map Controller done");
    }

    @Override
    public void updateContent() {
        updateMap();
    }

    @FXML
    private void updMapBtnPressed(ActionEvent event) {
        updateMap();
    }

    private void updateMap() {
        String response[] = gameServerDriver.sendLOOK(stateManager.getCurrentGameName());

        if (response[0].equals("OK")){
            char[][] charMap = stringToCharMap(response[1]);
            drawMap(charMap);
        }
        else {
            canvasContext.setStroke(Color.RED);
            canvasContext.strokeText("CANVAS ERROR!", 314, 314);
        }
    }

    private char[][] stringToCharMap(String mapResponse) {
        // Quick fix - improvements coming soon directly on GameServerDriver
        // Map is currently limited to 32x32 - increasing in the future
        mapResponse = mapResponse.replace("LONG\n", "");

        char[][] parsedMap = new char[32][32];

        int i = 0; // String index

        for(int r=0; r<32; r++) // rows
            for(int c=0; c<32; c++) { // columns
                if(mapResponse.charAt(i) == '\n') // skip this character
                    i++;

                parsedMap[r][c] = mapResponse.charAt(i);

                i++; // now we need to increase anyways
            }

        return parsedMap;
    }

    private void drawMap(char[][] charMap) {
        int rowCoordinate = 0, columnCoordinate = 0, cellDimension = 15;

        for(int r=0; r<32; r++) { // rows
            for (int c = 0; c < 32; c++) { // columns
                setColor(charMap[r][c]);

                canvasContext.fillRect(rowCoordinate, columnCoordinate, cellDimension, cellDimension);

                columnCoordinate += cellDimension;
            }
            rowCoordinate += cellDimension;
            columnCoordinate = 0;
        }
    }

    private void setColor(char terrain) {
        switch (terrain) {
            case '.': // grass
                canvasContext.setFill(Color.GREEN);
                break;
            case '#': // wall
                canvasContext.setFill(Color.GRAY);
                break;
            case '~': // river
                canvasContext.setFill(Color.CYAN);
                break;
            case '@': // ocean
                canvasContext.setFill(Color.BLUE);
                break;
            case '!': // trap
                canvasContext.setFill(Color.FIREBRICK);
                break;
            case '$': // energy recharge
                canvasContext.setFill(Color.YELLOW);
                break;
            case '&': // barrier
                canvasContext.setFill(Color.BROWN);
                break;
            case 'x': // flags - TODO distinguish color by team
            case 'X':
                canvasContext.setFill(Color.ORANGE);
                break;
            default: // player - TODO distinguish teams and current player from others
                canvasContext.setFill(Color.MAGENTA);
        }
    }
}
